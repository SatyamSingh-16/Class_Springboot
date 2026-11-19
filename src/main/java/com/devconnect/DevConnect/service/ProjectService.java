package com.devconnect.DevConnect.service;

import com.devconnect.DevConnect.model.Project;

import java.util.List;

public interface ProjectService {
    List<Project> getAllProjects();

    Project getProjectById(Long id);

    Project createProject(Project project);
}
