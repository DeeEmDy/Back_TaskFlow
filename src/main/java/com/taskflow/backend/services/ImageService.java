package com.taskflow.backend.services;

import java.util.Optional;

import com.taskflow.backend.entities.Image;
import com.taskflow.backend.exception.ImageNotFoundException;
import com.taskflow.backend.repositories.ImageRepository;

public class ImageService {

    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public Image getImageById(Integer id) {
        Optional<Image> image = imageRepository.findById(id);
        if (image.isPresent()) {
            return image.get();
        } else {
            throw new ImageNotFoundException("Image not found with id: " + id);
        }
    }
}