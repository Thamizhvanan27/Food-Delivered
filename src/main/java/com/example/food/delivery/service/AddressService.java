package com.example.food.delivery.service;

import com.example.food.delivery.dto.AddressDto;
import com.example.food.delivery.entity.Address;
import com.example.food.delivery.entity.User;
import com.example.food.delivery.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public List<Address> getUserAddresses(User user) {
        return addressRepository.findByUserOrderByIdDesc(user);
    }

    public Address getAddressById(Long id, User user) {
        return addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Address not found or unauthorized"));
    }

    @Transactional
    public Address saveAddress(User user, AddressDto dto) {
        if (dto.isDefault()) {
            // Unset current default
            List<Address> addresses = addressRepository.findByUserOrderByIdDesc(user);
            addresses.forEach(a -> a.setDefault(false));
            addressRepository.saveAll(addresses);
        }

        Address address;
        if (dto.getId() != null) {
            address = getAddressById(dto.getId(), user);
        } else {
            address = new Address();
            address.setUser(user);
        }

        address.setFullName(dto.getFullName());
        address.setPhone(dto.getPhone());
        address.setHouseNo(dto.getHouseNo());
        address.setAddressLine(dto.getAddressLine());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPincode(dto.getPincode());
        address.setLandmark(dto.getLandmark());
        address.setDefault(dto.isDefault());

        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long id, User user) {
        Address address = getAddressById(id, user);
        addressRepository.delete(address);
    }
}
