package com.toolrental.application.category.command;

public record RegisterCategoryCommand(String name, String description) {
  public RegisterCategoryCommand(String name) {
    this(name, null);
  }
}
