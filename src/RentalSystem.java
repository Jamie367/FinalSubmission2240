import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
public class RentalSystem { 
    private static RentalSystem instance;

    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private RentalHistory rentalHistory = new RentalHistory();
     
    private RentalSystem() { loadData(); }

    public boolean  addVehicle(Vehicle vehicle) {
        for (Vehicle i : vehicles) {
            if(i.getLicensePlate().equals(vehicle.getLicensePlate())){
                return false;
            }
        }
        vehicles.add(vehicle);
        saveVehicle(vehicle);
        return true;

    }
    public static RentalSystem getInstance(){
        if(instance == null){
            instance = new RentalSystem();
        }
        return instance;
    }
    public boolean  addCustomer(Customer customer) {
        for (Customer i : customers) {
          if(i.getCustomerId() == customer.getCustomerId()){
            return false;
          }
        }
        
        customers.add(customer);
        saveCustomer(customer);
        return true;
    }

    public void rentVehicle(Vehicle vehicle, Customer customer, LocalDate date, double amount) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
            vehicle.setStatus(Vehicle.VehicleStatus.RENTED);
            RentalRecord record = new RentalRecord(vehicle, customer, date, amount, "RENT");
            rentalHistory.addRecord(record);
            saveRecord(record);
            System.out.println("Vehicle rented to " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not available for renting.");
        }
    }

    public void returnVehicle(Vehicle vehicle, Customer customer, LocalDate date, double extraFees) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.RENTED) {
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            RentalRecord record = new RentalRecord(vehicle, customer, date, extraFees, "RETURN");
            rentalHistory.addRecord(record);
            saveRecord(record);
            System.out.println("Vehicle returned by " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not rented.");
        }
    }    

    public void displayVehicles(boolean onlyAvailable) {
    	System.out.println("|     Type         |\tPlate\t|\tMake\t|\tModel\t|\tYear\t|");
    	System.out.println("---------------------------------------------------------------------------------");
    	 
        for (Vehicle v : vehicles) {
            if (!onlyAvailable || v.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
                
                System.out.println("|     " + (v instanceof Car ? "Car          " : "Motorcycle   ") + "|\t" + v.getLicensePlate() + "\t|\t" + v.getMake() + "\t|\t" + v.getModel() + "\t|\t" + v.getYear() + "\t|\t");
                
            }
        }
        System.out.println();
    }
    
    public void displayAllCustomers() {
        for (Customer c : customers) {
            System.out.println("  " + c.toString());
        }
    }
    
    public void displayRentalHistory() {
        for (RentalRecord record : rentalHistory.getRentalHistory()) {
            System.out.println(record.toString());
        }
    }
    
    public Vehicle findVehicleByPlate(String plate) {
        for (Vehicle v : vehicles) {
            if (v.getLicensePlate().equalsIgnoreCase(plate)) {
                return v;
            }
        }
        return null;
    }
    
    public Customer findCustomerById(String id) {
        for (Customer c : customers)
            if (c.getCustomerId() == Integer.parseInt(id))
                return c;
        return null;
    }
    public void saveVehicle(Vehicle vehicle){
        try{
        FileWriter vehWriter = new FileWriter("vehicles.txt");
        vehWriter.append(vehicle.getInfo());
        vehWriter.append("\n");
        vehWriter.close();
        } catch(IOException e){
            System.out.println("error writing to vehicle file");
        } 

    }
    public void saveCustomer(Customer customer){
        try{
            FileWriter cusWriter = new FileWriter("customers.txt");
            cusWriter.append(customer.printable());
            cusWriter.append("\n");
            cusWriter.close();
            } catch(IOException e){
                System.out.println("error writing to Customer file");
            } 
    }
    public void saveRecord(RentalRecord record){
        try{
            FileWriter recWriter = new FileWriter("rental_records.txt");
            recWriter.append(record.toString());
            recWriter.append("\n");
            recWriter.close();
            } catch(IOException e){
                System.out.println("error writing to record file");
            } 
    }
    
    private void loadData(){
        try {
            Scanner vehRead = new Scanner("vehicles.txt");
        Scanner cusRead = new Scanner("customers.txt");
        Scanner rentRead = new Scanner("rental_records.txt");
        int iterator = 0;
            while(cusRead.hasNextLine()){
                String[] newAddition;
                newAddition = cusRead.nextLine().split(",");
                    customers.add(new Customer( Integer.valueOf(newAddition[0]), newAddition[1]));
            }
            cusRead.close();
            iterator = 0;
            while(vehRead.hasNextLine()){
                String line = vehRead.nextLine();
                String[] parts = line.split(" //| ");
                     // Extract common vehicle info
                String licensePlate = parts[1].trim();
                String make = parts[2].trim();
                String model = parts[3].trim();
                int year = Integer.parseInt(parts[4].trim());
                
                
                // Check what specific info is there
                if (line.contains("Cargo Capacity")) {
                    // Truck
                    int cargoCapacity = Integer.parseInt(parts[6].split(":")[1].trim());
                    Vehicle newTruck = new Truck(make, model, year, cargoCapacity);
                    newTruck.setLicensePlate(licensePlate);
                    vehicles.add(newTruck);

                } else if (line.contains("Seats:")) {
                    // car
                    int seats = Integer.parseInt(parts[6].split(":")[1].trim());
                    Vehicle newCar = new Car(make, model, year, seats);
                    newCar.setLicensePlate(licensePlate);
                    vehicles.add(newCar);
                } else if (line.contains("Sidecar")) {
                    // Motorcycle
                    boolean hasSidecar = parts[6].split(":")[1].trim().equalsIgnoreCase("Yes");
                    Vehicle bike = new Motorcycle( make, model, year, hasSidecar);
                    bike.setLicensePlate(licensePlate);
                    vehicles.add(bike);

                }
            }
            vehRead.close();
           
            while(rentRead.hasNextLine()){
                String[] parts = rentRead.nextLine().split(" //| ");
                
                String plate = parts[1].replace("Plate: ", "");
                String customerName = parts[2].replace("Customer: ", "");
                String date = parts[3].replace("Date: ", "");
                String amount = parts[4].replace("Amount: $", "");

                Vehicle newVehicle = findVehicleByPlate(plate);
                Customer newCustomer = findCustomerById(customerName);
                LocalDate newDate = LocalDate.parse(date);
                double newAmount = Double.parseDouble(amount);
                rentalHistory.addRecord(new RentalRecord(newVehicle,newCustomer,newDate,newAmount,parts[0]));

            }
            rentRead.close();
        } catch (Exception e) {
        }
        
    }
}