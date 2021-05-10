package dhbw.smartmoderation.data.model;

public enum Attendance {

    PRESENT (0), EXCUSED(1), ABSENT(2);

    private int number;

    Attendance(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}