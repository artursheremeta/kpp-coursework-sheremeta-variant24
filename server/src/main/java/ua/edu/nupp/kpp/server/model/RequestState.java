package ua.edu.nupp.kpp.server.model;

/** Database/API state names; exact Ukrainian labels in the client. */
public enum RequestState {
    NEW, ASSESSED, IN_PROGRESS, COMPLETED;

    public boolean canTransitionTo(RequestState next) {
        return next != null && switch (this) {
            case NEW -> next == ASSESSED;
            case ASSESSED -> next == IN_PROGRESS;
            case IN_PROGRESS -> next == COMPLETED;
            case COMPLETED -> false;
        };
    }
}
