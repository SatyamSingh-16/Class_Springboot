package com.devconnect.DevConnect.service;

import com.devconnect.DevConnect.model.Project;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final Map<Long, Project> projectStore = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public List<Project> getAllProjects() {
        return new ArrayList<>(projectStore.values());
    }

    @Override
    public Project getProjectById(Long id) {
        return projectStore.get(id);
    }

    @Override
    public Project createProject(Project project) {
        long id = idCounter.getAndIncrement();
        project.setId(id);
        projectStore.put(id, project);
        return project;
    }
}
