package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        // check train is valid or not
        Optional<Train> trainOptional = trainRepository.findById(bookTicketEntryDto.getTrainId());
        if(!trainOptional.isPresent()){
            throw new Exception("Invalid Train Id");
        }

        // if it is present then get the train
        Train train = trainOptional.get();


        // check for bookings
        List<Ticket> ticketList = train.getBookedTickets();
        int totalNoOfSeats = train.getNoOfSeats();
        if((totalNoOfSeats - ticketList.size()) < bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }

        // if seats are enough then book the tickets
        Ticket bookTicket = new Ticket();

        // check these passengers are valid or not
        List<Integer> passengerIds = bookTicketEntryDto.getPassengerIds();
        List<Passenger> passengerList = new ArrayList<>();
        for(Integer id : passengerIds){
            Optional<Passenger> passengerOptional = passengerRepository.findById(id);
            if(!passengerOptional.isPresent()){
                throw new Exception("Invalid person Id");
            }else {
                passengerList.add(passengerOptional.get());
            }
        }

        // set the passenger list
        bookTicket.setPassengersList(passengerList);


        // check that this train has the same route or not
        String str = train.getRoute();
        String[] route = str.split(",");

        int fromStationIdx = -1;
        int toStationIdx = -1;

        for(int i = 0; i< route.length; i++){
            String currStation = route[i];
            if(currStation.equals(bookTicketEntryDto.getFromStation())){
                fromStationIdx = i;
            } else if (currStation.equals(bookTicketEntryDto.getToStation())) {
                toStationIdx = i;
            }
        }

        if(fromStationIdx == -1 ){
            throw new Exception("Invalid stations-1");
        }
        if(toStationIdx == -1 ){
            throw new Exception("Invalid stations-2");
        }
        if(toStationIdx - fromStationIdx  < 0){
            throw new Exception("Invalid stations-3");
        }

        bookTicket.setFromStation(bookTicketEntryDto.getFromStation());
        bookTicket.setToStation(bookTicketEntryDto.getToStation());

        // set the fare
        int stationsToCover = toStationIdx - fromStationIdx;
        int fare =  stationsToCover * 300;
        bookTicket.setTotalFare(fare);
        bookTicket.setTrain(train);

        // changes done after setting the train in ticket
        train.getBookedTickets().add(bookTicket);
        train.setNoOfSeats(train.getNoOfSeats() - bookTicketEntryDto.getNoOfSeats());

        // changes done after setting the passenger in ticket
        Optional<Passenger> passengerOptional = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
        if(!passengerOptional.isPresent()){
            throw  new Exception("Invalid Booking Id");
        }

        // else get the passenger
        Passenger passenger = passengerOptional.get();
        passenger.getBookedTickets().add(bookTicket); // add the tickets in the passenger

        // save the train it will save both
        trainRepository.save(train);

        return ticketRepository.save(bookTicket).getTicketId();

    }
}
