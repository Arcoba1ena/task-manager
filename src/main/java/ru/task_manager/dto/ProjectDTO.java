package ru.task_manager.dto;

public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private Long createdById;

    // Конструкторы
    public ProjectDTO() {}

    public ProjectDTO(String name, String description, Long createdById) {
        this.name = name;
        this.description = description;
        this.createdById = createdById;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
}
