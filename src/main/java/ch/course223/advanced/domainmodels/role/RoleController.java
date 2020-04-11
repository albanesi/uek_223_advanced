package ch.course223.advanced.domainmodels.role;

import ch.nyp.noa.webContext.domain.role.mapper.RoleMapperExtended;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {
	
	private RoleService roleService;

	private RoleMapperExtended roleMapper;

	@Autowired
	public RoleController(RoleService roleservice, RoleMapperExtended roleMapper) {
		this.roleService = roleservice;
		this.roleMapper = roleMapper;
	}

	@GetMapping({"/{id}", "/{id}/"})
	@PreAuthorize("hasAuthority('ROLE_SEE_GLOBAL')")
	public ResponseEntity<RoleDTO> getById(@PathVariable String id) {
		Role role = roleService.findById(id);
		return new ResponseEntity<>(roleMapper.toDTO(role), HttpStatus.OK);
	}

	@GetMapping({"", "/"})
	@PreAuthorize("hasAuthority('ROLE_SEE_GLOBAL')")
	public @ResponseBody
    ResponseEntity<List<RoleDTO>> getAll() {
		List<Role> roles = roleService.findAll();
		return new ResponseEntity<>(roleMapper.toDTOs(roles), HttpStatus.OK);
	}

	@PostMapping({"", "/"})
	@PreAuthorize("hasAuthority('ROLE_CREATE')")
	public ResponseEntity<RoleDTO> create(@Valid @RequestBody RoleDTO roleDTO) {
		Role role = roleService.save(roleMapper.fromDTO(roleDTO));
		return new ResponseEntity<>(roleMapper.toDTO(role), HttpStatus.CREATED);
	}

	@PostMapping({"/{roleId}/authorities/{authorityId}", "/{roleId}/authorities/{authorityId}/"})
	@PreAuthorize("hasAuthority('ROLE_ADD_AUTHORITY')")
	public ResponseEntity<RoleDTO> addAuthorityToRole(@PathVariable String roleId, @PathVariable String authorityId) {
		Role role = roleService.addAuthorityToRole(roleId, authorityId);
		return new ResponseEntity<>(roleMapper.toDTO(role), HttpStatus.CREATED);
	}

	@PutMapping({"/{id}", "/{id}/"})
	@PreAuthorize("hasAuthority('ROLE_MODIFY')")
	public ResponseEntity<RoleDTO> updateById(@PathVariable String id, @RequestBody RoleDTO roleDTO) {
		Role role = roleService.updateById(id, roleMapper.fromDTO(roleDTO));
		return new ResponseEntity<>(roleMapper.toDTO(role), HttpStatus.OK);
	}

	@DeleteMapping({"/{id}", "/{id}/"})
	@PreAuthorize("hasAuthority('ROLE_DELETE')")
	public ResponseEntity<Void> deleteById(@PathVariable String id) {
		roleService.deleteById(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@DeleteMapping({"/{roleId}/authorities/{authorityId}", "/{roleId}/authorities/{authorityId}/"})
	@PreAuthorize("hasAuthority('ROLE_REMOVE_AUTHORITY')")
	public ResponseEntity<RoleDTO> removeAuthorityFromRole(@PathVariable String roleId, @PathVariable String authorityId) {
		Role role = roleService.removeAuthorityFromRole(roleId, authorityId);
		return new ResponseEntity<>(roleMapper.toDTO(role), HttpStatus.NO_CONTENT);
	}
	
}
