package com.toolrental.application.user.command;

public record RegisterUserCommand(String email, String displayName, String password) {}
