package ch.course223.advanced.domainmodels.role.mapper;

import ch.nyp.noa.config.generic.ExtendedDTOMapper;
import ch.nyp.noa.webContext.domain.role.Role;
import ch.nyp.noa.webContext.domain.role.RoleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel="spring", unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface RoleMapperExtended extends ExtendedDTOMapper<Role, RoleDTO> {
	
}
