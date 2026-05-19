package com.spiky.jsonflattener;

import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class JsonFlattenerModel {

    public String sourceDirectory;
    public String destinationDirectory;
    public int jsonFilesFound;
    public int jsonFilesSuccess;
    public int jsonFilesFail;
    public int otherFilesFound;
    public boolean ioExceptionFlag = false;
    public List<Path> jsonFileList = new ArrayList<>();


    public JsonFlattenerModel() {
        sourceDirectory = System.getProperty("user.dir");
        destinationDirectory = sourceDirectory+"\\JsonFlattenerOutput";
    }

    public void fileScan() throws IOException {
        jsonFilesFound = 0;
        otherFilesFound = 0;
        Path source = Paths.get(sourceDirectory);
        jsonFileList.clear();
        try (Stream<Path> stream=Files.walk(source)){
            stream.forEach(path -> {
                if (Files.isRegularFile(path)){
                    if (path.toString().toLowerCase().endsWith(".json")){
                        jsonFilesFound++;
                        jsonFileList.add(path);
                    } else {
                        otherFilesFound++;
                    }
                }
            });
        }
    }
    public void fileFlatten() {
        jsonFilesSuccess = 0;
        jsonFilesFail = 0;
        Path sourceDir = Paths.get(sourceDirectory);
        Path destinationDir = Paths.get(destinationDirectory);
        ObjectMapper mapper = JsonMapper.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build();
        jsonFileList.forEach( path ->{
            Path relativeJson = sourceDir.relativize(path);
            String fileName = path.getFileName().toString();
            String txtName = fileName.substring(0, fileName.lastIndexOf('.')) + ".txt";
            Path parent = relativeJson.getParent();
            Path destinationPath;
            if (parent == null) {
                destinationPath = destinationDir.resolve(txtName);
            } else {
                destinationPath = destinationDir.resolve(parent).resolve(txtName);
            }
            try {
                JsonNode rootNode = mapper.readTree(Files.readString(path, StandardCharsets.UTF_8));
                String outputText = formatJson(rootNode, "");

                if (outputText.endsWith(",\n")) {
                    outputText = outputText.substring(0, outputText.length() - 2);
                }

                Files.createDirectories(destinationPath.getParent());
                Files.writeString(destinationPath, outputText, StandardCharsets.UTF_8);
                jsonFilesSuccess++;
            } catch (Exception e){
                jsonFilesFail++;
                String outputText  = "--- ERROR: Could not parse " +
                        path.getFileName() + ". It may not be valid JSON. ---\n" +
                        "Error Message: " + e.getMessage();
                try {
                    Path errorPath = destinationDir.resolve("0ERROR").resolve("0ERROR_"+txtName);
                    Files.createDirectories(errorPath.getParent());
                    Files.writeString(errorPath, outputText, StandardCharsets.UTF_8);
                } catch (IOException ioException) {
                    ioExceptionFlag = true;
                    Path ioExceptionPath = destinationDir.resolve("IOException.log");
                    try {
                        Files.writeString(ioExceptionPath, ioException.getMessage(), StandardCharsets.UTF_8);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    private String formatJson(JsonNode rootNode, String prefix){
        StringBuilder sb = new StringBuilder();
        if (rootNode.isObject()) {
            for (Map.Entry<String, JsonNode> entry : rootNode.properties()) {
                JsonNode value = entry.getValue();
                if (value.isObject() || value.isArray()) {
                    sb.append(prefix).append(entry.getKey()).append(":\n");
                    sb.append(formatJson(value, prefix + "-"));
                } else {
                    sb.append(prefix).append(entry.getKey()).append(": ").append(value.asString()).append(",\n");
                }
            }
        } else if (rootNode.isArray()) {
            for (JsonNode item : rootNode) {
                if (item.isObject() || item.isArray()){
                    sb.append(formatJson(item, prefix));
                } else {
                    sb.append(prefix).append(item.asString()).append(",\n");
                }
            }
        } else {
            sb.append(prefix).append(rootNode.asString()).append(",\n");
        }
        return sb.toString();
    }

}
