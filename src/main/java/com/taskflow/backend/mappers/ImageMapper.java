package com.taskflow.backend.mappers;

import org.mapstruct.Mapper;

import com.taskflow.backend.dto.ImageDto;
import com.taskflow.backend.entities.Image;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    ImageDto toImageDto(Image image);
    Image toImage(ImageDto imageDto);
}
