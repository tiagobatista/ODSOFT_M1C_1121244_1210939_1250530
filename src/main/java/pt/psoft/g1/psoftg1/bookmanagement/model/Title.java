package pt.psoft.g1.psoftg1.bookmanagement.model;

import lombok.Getter;

public class Title {

    public static final int TITLE_MAX_LENGTH = 128;

    @Getter
    private String title;

    public Title() {
    }

    public Title(String title) {
        setTitle(title);
    }

    public void setTitle(String title) {
        if (title == null)
            throw new IllegalArgumentException("Title cannot be null");
        if (title.isBlank())
            throw new IllegalArgumentException("Title cannot be blank");
        if (title.length() > TITLE_MAX_LENGTH)
            throw new IllegalArgumentException("Title has a maximum of " + TITLE_MAX_LENGTH + " characters");
        this.title = title.strip();
    }

    @Override
    public String toString() {
        return this.title;
    }
}