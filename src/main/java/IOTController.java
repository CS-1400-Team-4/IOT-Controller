import java.util.ArrayList;

public abstract class IOTController {
    public record InputState(int temperature, int time, int month, boolean isSomeoneHome) {}

    @FunctionalInterface
    public interface IOTDevice {
        boolean isActive(InputState newState, InputState previousState);
    }

    public static final int SET_POINT_TEMP = 70;

    // If it is between 9am and 6pm, and it's not too hot, open blinds
    public static IOTDevice BLINDS = (current, prev) -> current.time() > 9 && current.time() < 18 && current.temperature() < SET_POINT_TEMP;

    // If it is in the cold half of the year, heat home to set point if someone is home
    // Checks current and previous temperatures to avoid fast cycling
    public static IOTDevice HEATER = (current, prev) -> {
        if (current.temperature() < SET_POINT_TEMP && prev.temperature() < SET_POINT_TEMP
                && current.month() > 4 && current.month() < 10
                && current.isSomeoneHome())
        {
            return true;
        }

        return false;
    };

    // If it is in the warm half of the year, cool home to set point if someone is home
    // Checks current and previous temperatures to avoid fast cycling
    public static IOTDevice AC = (current, prev) -> {
        if (current.temperature() < SET_POINT_TEMP && prev.temperature() < SET_POINT_TEMP
                && (current.month() <= 4 || current.month() >= 10)
                && current.isSomeoneHome())
        {
            return true;
        }

        return false;
    };

    // Turn lights on if people are home in the evening
    public static IOTDevice LIGHTS = (current, prev) -> {
        // Turn on lights immediately if someone gets home, but leave them on briefly after leaving to
        // avoid accidentally shutting lights off on anyone
        if (prev.isSomeoneHome() || current.isSomeoneHome()) {
            return current.time() > 18 && current.time() < 22;
        }

        return false;
    };

    public static ArrayList<IOTDevice> devices = new ArrayList<>();

    public static void main(String[] args) {
        devices.add(BLINDS);
        devices.add(HEATER);
        devices.add(AC);
        devices.add(LIGHTS);
    }
}
