package com.toolrental.application.user.command;

public record ChangePasswordCommand(String currentPassword, String newPassword) {}
