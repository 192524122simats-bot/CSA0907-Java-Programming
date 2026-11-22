import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Assignment 1 - Java Parking Management System
 * Single-file implementation. Save as Main.java and run with Java 21.
 */
public class Main {

    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        ParkingFacility facility = new ParkingFacility(new int[]{2, 3, 1, 2, 1});
        ParkingService service = new ParkingService(facility);

        while (true) {
            System.out.println("\n===== PARKING MANAGEMENT SYSTEM =====");
            System.out.println("1. Vehicle Entry");
            System.out.println("2. Vehicle Exit");
            System.out.println("3. Display Parking Slots");
            System.out.println("4. Daily Collection Report");
            System.out.println("0. Exit");
            int choice = readInt("Enter choice: ");

            try {
                switch (choice) {
                    case 1 -> vehicleEntry(service);
                    case 2 -> vehicleExit(service);
                    case 3 -> facility.displaySlots();
                    case 4 -> service.printDailyReport();
                    case 0 -> {
                        System.out.println("Thank you.");
                        return;
                    }
                    default -> System.out.println("Invalid menu choice.");
                }
            } catch (IllegalArgumentException | IllegalStateException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static void vehicleEntry(ParkingService service) {
        System.out.println("\nVehicle types: bike, car, ev, ebike, van");
        String type = readText("Enter vehicle type: ");
        String regNo = readText("Enter registration number: ");
        ParkingTicket ticket = service.enter(type, regNo, LocalTime.now());

        System.out.println("Vehicle parked successfully.");
        System.out.println("Ticket number : " + ticket.getTicketNo());
        System.out.println("Registration  : " + ticket.getVehicle().getRegNo());
        System.out.println("Floor         : " + ticket.getFloor());
        System.out.println("Slot          : " + ticket.getSlot());
        System.out.println("Entry time    : " + ticket.getEntry());
    }

    private static void vehicleExit(ParkingService service) {
        String regNo = readText("Enter registration number: ");
        int minutes = readInt("Enter parking duration in minutes: ");
        int kWh = readInt("Enter EV charging units (enter 0 if not applicable): ");

        Pass pass = null;
        String passChoice = readText("Pass type (none/monthly/oneday): ").toLowerCase();

        if (passChoice.equals("monthly")) {
            LocalDate from = readDate("Monthly pass valid from (YYYY-MM-DD): ");
            LocalDate to = readDate("Monthly pass valid until (YYYY-MM-DD): ");
            pass = new MonthlyPass(from, to);
        } else if (passChoice.equals("oneday")) {
            LocalDate day = readDate("One-day pass date (YYYY-MM-DD): ");
            pass = new OneDayPass(day, 50.0);
        } else if (!passChoice.equals("none") && !passChoice.isEmpty()) {
            throw new IllegalArgumentException("Unknown pass type.");
        }

        double fee = service.exit(regNo, minutes, kWh, pass, LocalDate.now());
        System.out.printf("Total parking/charging fee: Rs %.2f%n", fee);
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return SC.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SC.nextLine().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        try {
            return LocalDate.parse(readText(prompt));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Date must be in YYYY-MM-DD format.");
        }
    }

    interface Chargeable {
        double chargingFee(int kWh);
    }

    interface Pass {
        boolean covers(LocalDate day);
    }

    static abstract class Vehicle {
        private final String regNo;

        protected Vehicle(String regNo) {
            if (regNo == null || regNo.trim().isEmpty()) {
                throw new IllegalArgumentException("Registration number required");
            }
            this.regNo = regNo.trim().toUpperCase();
        }

        public String getRegNo() {
            return regNo;
        }

        protected abstract double ratePerHour();

        public double calculateFee(int minutes) {
            if (minutes < 0) {
                throw new IllegalArgumentException("Duration cannot be negative");
            }
            if (minutes == 0) {
                return 0.0;
            }
            int hours = (minutes + 59) / 60;
            return Math.min(hours, 8) * ratePerHour();
        }

        public double calculateFee(LocalTime entry, int minutes) {
            return calculateFee(minutes);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Vehicle other && regNo.equals(other.regNo);
        }

        @Override
        public int hashCode() {
            return regNo.hashCode();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + regNo + "]";
        }
    }

    static class TwoWheeler extends Vehicle {
        TwoWheeler(String regNo) {
            super(regNo);
        }

        @Override
        protected double ratePerHour() {
            return 10.0;
        }
    }

    static class Car extends Vehicle {
        Car(String regNo) {
            super(regNo);
        }

        @Override
        protected double ratePerHour() {
            return 30.0;
        }
    }

    static class ElectricCar extends Car implements Chargeable {
        ElectricCar(String regNo) {
            super(regNo);
        }

        @Override
        public double chargingFee(int kWh) {
            if (kWh < 0) {
                throw new IllegalArgumentException("Charging units cannot be negative");
            }
            return kWh * 12.0;
        }
    }

    static class EBike extends Vehicle {
        EBike(String regNo) {
            super(regNo);
        }

        @Override
        protected double ratePerHour() {
            return 0.0;
        }

        @Override
        public double calculateFee(int minutes) {
            if (minutes < 0) {
                throw new IllegalArgumentException("Duration cannot be negative");
            }
            return minutes == 0 ? 0.0 : 20.0;
        }
    }

    static class DeliveryVan extends Car {
        DeliveryVan(String regNo) {
            super(regNo);
        }

        @Override
        public double calculateFee(LocalTime entry, int minutes) {
            boolean peak = !entry.isBefore(LocalTime.of(9, 0))
                    && entry.isBefore(LocalTime.of(17, 0));
            return calculateFee(minutes) * (peak ? 2.0 : 1.0);
        }
    }

    static class MonthlyPass implements Pass {
        private final LocalDate from;
        private final LocalDate to;

        MonthlyPass(LocalDate from, LocalDate to) {
            if (from == null || to == null || to.isBefore(from)) {
                throw new IllegalArgumentException("Invalid monthly pass dates");
            }
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean covers(LocalDate day) {
            return !day.isBefore(from) && !day.isAfter(to);
        }
    }

    static class OneDayPass implements Pass {
        private final LocalDate day;
        private final double price;

        OneDayPass(LocalDate day, double price) {
            if (day == null || price < 0) {
                throw new IllegalArgumentException("Invalid one-day pass");
            }
            this.day = day;
            this.price = price;
        }

        @Override
        public boolean covers(LocalDate date) {
            return day.equals(date);
        }

        public double getPrice() {
            return price;
        }
    }

    static class ParkingTicket {
        private static int counter = 0;

        private final int ticketNo;
        private final Vehicle vehicle;
        private final int floor;
        private final int slot;
        private final LocalTime entry;

        ParkingTicket(Vehicle vehicle, int floor, int slot, LocalTime entry) {
            if (vehicle == null || entry == null || floor < 0 || slot < 0) {
                throw new IllegalArgumentException("Invalid ticket details");
            }
            this.ticketNo = ++counter;
            this.vehicle = vehicle;
            this.floor = floor;
            this.slot = slot;
            this.entry = entry;
        }

        ParkingTicket(Vehicle vehicle, int floor, int slot) {
            this(vehicle, floor, slot, LocalTime.now());
        }

        ParkingTicket(ParkingTicket ticket) {
            this.ticketNo = ticket.ticketNo;
            this.vehicle = ticket.vehicle;
            this.floor = ticket.floor;
            this.slot = ticket.slot;
            this.entry = ticket.entry;
        }

        public int getTicketNo() {
            return ticketNo;
        }

        public Vehicle getVehicle() {
            return vehicle;
        }

        public int getFloor() {
            return floor;
        }

        public int getSlot() {
            return slot;
        }

        public LocalTime getEntry() {
            return entry;
        }
    }

    static class ParkingFacility {
        private final String[][] slots;

        ParkingFacility(int[] slotsPerFloor) {
            if (slotsPerFloor == null || slotsPerFloor.length == 0) {
                throw new IllegalArgumentException("At least one floor is required");
            }
            slots = new String[slotsPerFloor.length][];
            for (int floor = 0; floor < slotsPerFloor.length; floor++) {
                if (slotsPerFloor[floor] < 1) {
                    throw new IllegalArgumentException("Each floor must have at least one slot");
                }
                slots[floor] = new String[slotsPerFloor[floor]];
            }
        }

        public int[] allocate(String vehicleNo) {
            for (int floor = 0; floor < slots.length; floor++) {
                for (int slot = 0; slot < slots[floor].length; slot++) {
                    if (slots[floor][slot] == null) {
                        slots[floor][slot] = vehicleNo;
                        return new int[]{floor, slot};
                    }
                }
            }
            return null;
        }

        public boolean release(int floor, int slot) {
            if (floor < 0 || floor >= slots.length) {
                return false;
            }
            if (slot < 0 || slot >= slots[floor].length) {
                return false;
            }
            if (slots[floor][slot] == null) {
                return false;
            }
            slots[floor][slot] = null;
            return true;
        }

        public void displaySlots() {
            System.out.println("\n----- PARKING SLOT STATUS -----");
            for (int floor = 0; floor < slots.length; floor++) {
                System.out.print("Floor " + floor + ": ");
                for (int slot = 0; slot < slots[floor].length; slot++) {
                    System.out.print("[" + slot + ": "
                            + (slots[floor][slot] == null ? "FREE" : slots[floor][slot])
                            + "] ");
                }
                System.out.println();
            }
        }
    }

    static class VehicleFactory {
        static Vehicle create(String type, String regNo) {
            if (type == null) {
                throw new IllegalArgumentException("Vehicle type required");
            }
            return switch (type.trim().toLowerCase()) {
                case "bike", "twowheeler", "two-wheeler" -> new TwoWheeler(regNo);
                case "car" -> new Car(regNo);
                case "ev", "electriccar", "electric-car" -> new ElectricCar(regNo);
                case "ebike", "e-bike" -> new EBike(regNo);
                case "van", "deliveryvan", "delivery-van" -> new DeliveryVan(regNo);
                default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
            };
        }
    }

    static class ParkingService {
        private final ParkingFacility facility;
        private final Map<String, ParkingTicket> active = new HashMap<>();
        private double dailyCollection = 0.0;
        private LocalDate reportDate = LocalDate.now();

        ParkingService(ParkingFacility facility) {
            this.facility = facility;
        }

        public ParkingTicket enter(String type, String regNo, LocalTime time) {
            Vehicle vehicle = VehicleFactory.create(type, regNo);
            String key = vehicle.getRegNo();

            if (active.containsKey(key)) {
                throw new IllegalStateException("Already parked: " + key);
            }

            int[] position = facility.allocate(key);
            if (position == null) {
                throw new IllegalStateException("Facility full");
            }

            ParkingTicket ticket = new ParkingTicket(
                    vehicle, position[0], position[1], time);
            active.put(key, ticket);
            return ticket;
        }

        public double exit(String regNo, int minutes, int kWh,
                           Pass pass, LocalDate today) {
            if (minutes < 0 || kWh < 0) {
                throw new IllegalArgumentException("Duration and charging units cannot be negative");
            }
            if (regNo == null) {
                throw new IllegalArgumentException("Registration number required");
            }

            String key = regNo.trim().toUpperCase();
            ParkingTicket ticket = active.get(key);
            if (ticket == null) {
                throw new IllegalStateException("No active ticket for: " + key);
            }

            Vehicle vehicle = ticket.getVehicle();
            double fee = (pass != null && pass.covers(today))
                    ? 0.0
                    : vehicle.calculateFee(ticket.getEntry(), minutes);

            if (vehicle instanceof Chargeable chargeable) {
                fee += chargeable.chargingFee(kWh);
            }

            facility.release(ticket.getFloor(), ticket.getSlot());
            active.remove(key);

            if (!today.equals(reportDate)) {
                reportDate = today;
                dailyCollection = 0.0;
            }
            dailyCollection += fee;
            return fee;
        }

        public void printDailyReport() {
            System.out.println("\n----- DAILY COLLECTION REPORT -----");
            System.out.println("Date                 : " + reportDate);
            System.out.println("Currently parked     : " + active.size());
            System.out.printf("Total collection     : Rs %.2f%n", dailyCollection);
        }
    }
}
