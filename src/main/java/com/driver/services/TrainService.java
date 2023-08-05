package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.exception.TrainDoesNotExists;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        Train train = new Train();
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setDepartureTime(trainEntryDto.getDepartureTime());

        String route = getStringFromStationList(trainEntryDto.getStationRoute());
        train.setRoute(route);

        // save the train to db
        Train savedTrain = trainRepository.save(train);

        return savedTrain.getTrainId();
    }

    public String getStringFromStationList(List<Station> stationRoute){
        StringBuilder route = new StringBuilder();

        // add all the station to stringBuilder
        for (Station station : stationRoute){
            route.append(station.toString()).append(",");
        }

        // delete the last comma
        route.deleteCharAt(route.length()-1);

        return route.toString();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        List<Ticket> ticketList = train.getBookedTickets();
        String [] trainRoute = train.getRoute().split(",");
        HashMap<String ,Integer> map = new HashMap<>();
        for (int i =0; i <trainRoute.length ; i++){
            map.put(trainRoute[i] ,i);
        }
        if (!map.containsKey(seatAvailabilityEntryDto.getFromStation().toString()) || !map.containsKey(seatAvailabilityEntryDto.getToStation().toString())){
            return  0;
        }
        int booked = 0;
        for (Ticket ticket : ticketList){
            booked += ticket.getPassengersList().size();
        }
        int count = train.getNoOfSeats() - booked;
        for (Ticket ticket : ticketList){
            String fromStation = ticket.getFromStation().toString();
            String toStation = ticket.getToStation().toString();
            if (map.get(seatAvailabilityEntryDto.getToStation().toString()) <= map.get(fromStation)){
                count++;
            }else if (map.get(seatAvailabilityEntryDto.getFromStation().toString()) >= map.get(toStation)){
                count++;
            }
        }

        return count+2;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

//        Optional<Train> trainOptional = trainRepository.findById(trainId);
//        if(!trainOptional.isPresent()){
//            throw new TrainDoesNotExists("Invalid Train ID");
//        }
//
//        // else get the train
//        Train train = trainOptional.get();
//        String[] routes = train.getRoute().split(",");
//
//        String boardingStation = station.toString();
//
//        // iterate over the routes and get the people count
//        for(String route : routes){
//
//        }
//
//        return 0;

        Train train=trainRepository.findById(trainId).get();
        String reqStation=station.toString();
        String arr[]=train.getRoute().split(",");
        boolean found=false;

        for(String s:arr){
            if(s.equals(reqStation)){
                found=true;
                break;
            }
        }
        //if the trainId is not passing through that station

        if(found==false){
            throw new Exception("Train is not passing from this station");
        }

        int noOfPassengers=0;
        //throw new Exception("Train is not passing from this station");
        List<Ticket>ticketList= train.getBookedTickets();
        for(Ticket ticket:ticketList){
            if(ticket.getFromStation().toString().equals(reqStation)){
                noOfPassengers+=ticket.getPassengersList().size();
            }
        }


        //  in a happy case we need to find out the number of such people.


        return noOfPassengers;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        //check this train exists or not
        Optional<Train> trainOptional = trainRepository.findById(trainId);
        if(!trainOptional.isPresent()){
            throw new TrainDoesNotExists("Invalid Train ID");
        }

        // if it exists then get the train
        Train train = trainOptional.get();

        // get all the bookings for this train
        List<Ticket> ticketList = train.getBookedTickets();

        // if there's no person on the train then return 0
        if(ticketList.size() == 0){
            return 0;
        }

        // iterate over this list and find the older person
        int oldestPersonAge = Integer.MIN_VALUE;
        for (Ticket ticket : ticketList){
            List<Passenger> passengerList = ticket.getPassengersList();

            for (Passenger passenger : passengerList){
                oldestPersonAge = Math.max(oldestPersonAge,passenger.getAge());
            }
        }

        return oldestPersonAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> TrainList = new ArrayList<>();
        List<Train> trains = trainRepository.findAll();
        for(Train t:trains){
            String s = t.getRoute();
            String[] ans = s.split(",");
            for(int i=0;i<ans.length;i++){
                if(Objects.equals(ans[i], String.valueOf(station))){
                    int startTimeInMin = (startTime.getHour() * 60) + startTime.getMinute();
                    int lastTimeInMin = (endTime.getHour() * 60) + endTime.getMinute();


                    int departureTimeInMin = (t.getDepartureTime().getHour() * 60) + t.getDepartureTime().getMinute();
                    int reachingTimeInMin  = departureTimeInMin + (i * 60);
                    if(reachingTimeInMin>=startTimeInMin && reachingTimeInMin<=lastTimeInMin)
                        TrainList.add(t.getTrainId());
                }
            }
        }
        return TrainList;
    }

}

