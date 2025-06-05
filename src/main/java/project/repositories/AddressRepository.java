package project.repositories;

import configurations.genericsRepositories.GenericRepository;
import configurations.genericsRepositories.annotations.Repository;
import project.entities.Address;

@Repository
public interface AddressRepository extends GenericRepository<Address, Long> {
}
