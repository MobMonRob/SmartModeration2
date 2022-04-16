package dhbw.smartmoderation.data.model;

public enum Status {

    ANGELEGT("Angelegt", 0),
    OFFEN("Offen", 1),
    BEWERTET("Bewertet", 2),
    ABGESCHLOSSEN("Abgeschlossen", 3),
    DEAKTIVIERT ("Deaktiviert", 4);

    private String title;
    private int number;

    private Status(String title, int number) {
        this.title = title;
        this.number = number;
    }

    public String getTitle() { return this.title; }

    public int getNumber() { return this.number; }
}
