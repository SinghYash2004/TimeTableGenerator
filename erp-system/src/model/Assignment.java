package model;

public class Assignment {
    private Subject subject;
    private Teacher teacher;
    private Room room;

    public Assignment(Subject subject, Teacher teacher, Room room) {
        this.subject = subject;
        this.teacher = teacher;
        this.room = room;
    }

    public Subject getSubject() {
        return subject;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public Room getRoom() {
        return room;
    }
}
