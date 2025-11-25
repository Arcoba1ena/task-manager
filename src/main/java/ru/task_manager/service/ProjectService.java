package ru.task_manager.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ru.task_manager.entity.User;
import ru.task_manager.entity.Project;
import ru.task_manager.dto.ProjectDTO;
import org.springframework.stereotype.Service;
import ru.task_manager.dto.ProjectResponseDTO;
import ru.task_manager.dto.ProjectWithTasksDTO;
import ru.task_manager.repository.UserRepository;
import ru.task_manager.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    public Project createProject(ProjectDTO projectDTO) {
        Project project = new Project();
        return updateProjectFromDTO(project, projectDTO);
    }

    public Project updateProject(Long id, ProjectDTO projectDTO) {
        Optional<Project> existingProject = projectRepository.findById(id);
        if (existingProject.isPresent()) {
            Project project = existingProject.get();
            return updateProjectFromDTO(project, projectDTO);
        }
        return null;
    }

    private Project updateProjectFromDTO(Project project, ProjectDTO projectDTO) {
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());

        if (projectDTO.getCreatedById() != null) {
            Optional<User> createdBy = userRepository.findById(projectDTO.getCreatedById());
            createdBy.ifPresent(project::setCreatedBy);
        }

        return projectRepository.save(project);
    }

    public boolean deleteProject(Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public long getTotalProjectCount() {
        return projectRepository.count();
    }

    public List<ProjectResponseDTO> getAllProjectsWithDTO() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream()
                .map(ProjectResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<ProjectResponseDTO> getProjectByIdWithDTO(Long id) {
        return projectRepository.findById(id)
                .map(ProjectResponseDTO::fromEntity);
    }

    public Optional<ProjectWithTasksDTO> getProjectWithTasksById(Long id) {
        return projectRepository.findById(id)
                .map(ProjectWithTasksDTO::fromEntity);
    }
}