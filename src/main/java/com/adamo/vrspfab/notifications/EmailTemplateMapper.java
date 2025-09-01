package com.adamo.vrspfab.notifications;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmailTemplateMapper {
    
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByEmail", source = "createdBy.email")
    EmailTemplateDto toDto(EmailTemplate emailTemplate);
    
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModified", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    EmailTemplate toEntity(EmailTemplateDto dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModified", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    void updateFromDto(EmailTemplateDto dto, @MappingTarget EmailTemplate emailTemplate);
}
