import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public abstract class IOTController {
    public record InputState(int temperature, int time, int month, boolean isSomeoneHome) {}

    @FunctionalInterface
    public interface IOTLogic {
        boolean isActive(InputState newState, InputState previousState);
    }

    public record IOTDevice(String name, String activeMessage, String inactiveMessage, IOTLogic logic) {}

    public static final int SET_POINT_TEMP = 70;

    // If it is between 9am and 6pm, and it's not too hot, open blinds
    public static IOTDevice BLINDS = new IOTDevice("Blinds", "open", "closed",
            (current, prev) -> current.time() > 9 && current.time() < 18 && current.temperature() < SET_POINT_TEMP);

    // If it is in the cold half of the year, heat home to set point if someone is home
    // Checks current and previous temperatures to avoid fast cycling
    public static IOTDevice HEATER = new IOTDevice("Heater", "running", "off",
            (current, prev) -> {
                if (current.temperature() < SET_POINT_TEMP && prev.temperature() < SET_POINT_TEMP
                        && (current.month() <= 4 || current.month() >= 10)
                        && current.isSomeoneHome())
                {
                    return true;
                }

                return false;
            });

    // If it is in the warm half of the year, cool home to set point if someone is home
    // Checks current and previous temperatures to avoid fast cycling
    public static IOTDevice AC = new IOTDevice("Air Conditioning", "running", "off",
            (current, prev) -> {
                if (current.temperature() > SET_POINT_TEMP && prev.temperature() > SET_POINT_TEMP
                        && current.month() > 4 && current.month() < 10
                        && current.isSomeoneHome())
                {
                    return true;
                }

                return false;
            });

    // Turn lights on if people are home in the evening
    public static IOTDevice LIGHTS = new IOTDevice("Lights", "on", "off",
            (current, prev) -> {
                // Turn on lights immediately if someone gets home, but leave them on briefly after leaving to
                // avoid accidentally shutting lights off on anyone
                if (prev.isSomeoneHome() || current.isSomeoneHome()) {
                    return current.time() > 18 && current.time() < 22;
                }

                return false;
            });

    public static ArrayList<IOTDevice> devices = new ArrayList<>();
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        devices.add(BLINDS);
        devices.add(HEATER);
        devices.add(AC);
        devices.add(LIGHTS);

        InputState previousState = null;

        do {
            InputState currentState = getInputStateFromUser();
            if (previousState == null) {
                previousState = currentState;
            }

            System.out.println("Current state is: " + currentState);
            System.out.println("Previous state is: " + previousState);

            for (IOTDevice device : devices) {
                boolean isActive = device.logic.isActive(currentState, previousState);
                System.out.println("Device (" + device.name + ") is " + (isActive ? device.activeMessage : device.inactiveMessage));
            }

            previousState = currentState;
        } while(checkForContinue());
    }

    public static InputState getInputStateFromUser() {
        int time;
        try {
            System.out.println("What is the current time? Input as a number from 0-23: ");
            time = scanner.nextInt();
        }
        catch (InputMismatchException e) {
            time = -1;
        }

        while (time < 0 || time > 23) {
            System.out.println("Please enter a number from 0-23: ");
            time = scanner.nextInt();
        }

        int month;
        try {
            System.out.println("What is the current month? Input as a number from 0-12: ");
            month = scanner.nextInt();
        }
        catch (InputMismatchException e) {
            month = -1;
        }

        while (month < 0 || month > 12) {
            System.out.println("Please enter a number from 0-12: ");
            month = scanner.nextInt();
        }

        System.out.println("What is the current temperature? Input as a number in Fahrenheit: ");
        int temperature = scanner.nextInt();

        System.out.println("Is someone home right now? Y/N");
        boolean isSomeoneHome = scanner.next().equalsIgnoreCase("y");

        return new InputState(temperature, time, month, isSomeoneHome);
    }

    public static boolean checkForContinue() {
        System.out.println("Do you want to continue? Y/N");
        return scanner.next().equalsIgnoreCase("y");
    }
}
