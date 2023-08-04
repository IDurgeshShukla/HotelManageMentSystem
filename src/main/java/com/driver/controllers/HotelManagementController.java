package com.driver.controllers;

import com.driver.model.Booking;
import com.driver.model.Facility;
import com.driver.model.Hotel;
import com.driver.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.util.logging.LogManager.*;

@RestController
@RequestMapping("/hotel")
public class HotelManagementController {

    TreeMap<String, Hotel> hoteldb = new TreeMap<>();
    HashMap<Integer, User> Userdb = new HashMap<>();
    HashMap<String,Booking> bookings = new HashMap<>();
    HashMap<Integer,List<Booking>> bookingsByUser = new HashMap<>();
    private static final Logger logger = Logger.getLogger(HotelManagementController.class.getName());
    @PostMapping("/add-hotel")
    public ResponseEntity<String> addHotel(@RequestBody Hotel hotel){

        //need to add an hotel to the database
        //incase the hotelName is null or the hotel Object is null returning response entity according to that
        if ( hotel == null || hotel.getHotelName() == null) return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Invalid hotel or hotel name found");
        //Incase somebody is trying to add the duplicate hotelName it throws a  response entity
        if (hoteldb.containsKey(hotel.getHotelName())) return ResponseEntity.status(HttpStatus.CONFLICT).body(
                "Duplicate addition of Hotel.");
        //in all other cases return SUCCESS after successfully adding the hotel to the hotelDb.

            hoteldb.put(hotel.getHotelName(),hotel);

        return ResponseEntity.status(HttpStatus.OK).body("Hotel added Successfully");
    }

    @PostMapping("/add-user")
    public ResponseEntity<String> addUser(@RequestBody User user){

        // need to add a User Object to the database
        Integer aadharCard;
        try {
            aadharCard = user.getaadharCardNo();
            Userdb.put(aadharCard, user);
        }
        catch (Exception exception){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not have valid inputs. ");
        }
        //Assume that user will always be a valid user and return the aadharCardNo of the user

       return ResponseEntity.status(HttpStatus.CREATED).body(Userdb.get(aadharCard).getName() +" records added to " +
               "our database. ");
    }
    @GetMapping("/get-hotel-with-most-facilities")
    public String getHotelWithMostFacilities(){
        String validHotel = "";
        int mostfacility = 0;
        //Out of all the hotels we have added so far, we need to find the hotelName with most no of facilities
        for (String name : hoteldb.keySet()){
            if(hoteldb.get(name).getFacilities().size() > mostfacility){
                mostfacility = hoteldb.get(name).getFacilities().size();
                validHotel = name;
            }
        }
        //Incase there is a tie return the lexicographically smaller hotelName
        //Incase there is not even a single hotel with atleast 1 facility return "" (empty string)

        return validHotel;
    }
    private String generateUUID(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
    @PostMapping("/book-a-room")
    public ResponseEntity<String > bookARoom(@RequestBody Booking booking){

        //The booking object coming from postman will have all the attributes except bookingId and amountToBePaid;
        //Have bookingId as a random UUID generated String
         String bookingId =  generateUUID();

        //save the booking Entity and keep the bookingId as a primary key
        booking.setBookingId(bookingId);
        //Calculate the total amount paid by the person based on no. of rooms booked and price of the room per night.
        String hotelname = booking.getHotelName();
        if (hoteldb.get(hotelname).getAvailableRooms() < booking.getNoOfRooms()) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Available rooms in the Hotel is less than user requirement.");
        //If there arent enough rooms available in the hotel that we are trying to book return -1 
        //in other case return total amount paid
        int pricePerNightOfHotel = hoteldb.get(hotelname).getPricePerNight();
        int amount = booking.getNoOfRooms()*pricePerNightOfHotel;
        booking.setAmountToBePaid(amount);

        // add booking in bookingsByUser database
        List<Booking> list = bookingsByUser.getOrDefault(booking.getBookingAadharCard(), new ArrayList<>());
        list.add(booking);
        bookingsByUser.put(booking.getBookingAadharCard(),list);
        return ResponseEntity.status(HttpStatus.CREATED).body("Booking is done. Total amount to be paid by user: "+ amount +" .");
    }
    
    @GetMapping("/get-bookings-by-a-person/{aadharCard}")
    public int getBookings(@PathVariable("aadharCard")Integer aadharCard)
    {
        //In this function return the bookings done by a person
        int totalbookings = -1;
        try {
            totalbookings =  bookingsByUser.get(aadharCard).size();
        } catch (Exception e){
            logger.log(Level.SEVERE,"Invalid aadharCard");
        }
        return totalbookings;
    }

    @PutMapping("/update-facilities")
    public Hotel updateFacilities(List<Facility> newFacilities,String hotelName){

        //We are having a new facilites that a hotel is planning to bring.
        if (hoteldb.get(hotelName).getFacilities().equals(newFacilities)){

        } else{
            hoteldb.get(hotelName).setFacilities(newFacilities);
        }
        //If the hotel is already having that facility ignore that facility otherwise add that facility in the hotelDb
        //return the final updated List of facilities and also update that in your hotelDb
        //Note that newFacilities can also have duplicate facilities possible
        Hotel hotel = hoteldb.get(hotelName);
        return hotel;
    }

}
