package com.duoc.LearningPlatform.dto;

import jakarta.validation.constraints.NotNull;

public class SubmissionRequest {

    private String description;

    @NotNull(message = "fileName is required")
    private String fileName;

    @NotNull(message = "contentType is required")
    private String contentType;

    @NotNull(message = "fileContent is required")
    private byte[] fileContent;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }
}
