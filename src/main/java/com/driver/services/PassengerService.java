package com.driver.services;


import com.driver.model.Passenger;
import com.driver.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class PassengerService {

    @Autowired
    PassengerRepository passengerRepository;

    public Integer addPassenger(Passenger passenger){
        //Add the passenger Object in the passengerDb and return the passengerId that has been returned

        // create a passenger object
        Passenger newPassenger = new Passenger();
        newPassenger.setName(passenger.getName());
        newPassenger.setAge(passenger.getAge());
        newPassenger.setBookedTickets(new ArrayList<>());

        // add this passenger to db
        Passenger savedPassenger = passengerRepository.save(newPassenger);

        return savedPassenger.getPassengerId();
    }

}
