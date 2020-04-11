package ch.course223.advanced.domainmodels.authority.mapper;

import ch.nyp.noa.config.generic.ExtendedDTOMapper;
import ch.nyp.noa.webContext.domain.authority.Authority;
import ch.nyp.noa.webContext.domain.authority.AuthorityDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel="spring", unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface AuthorityMapperExtended extends ExtendedDTOMapper<Authority, AuthorityDTO> {
	
}