package ch.course223.advanced.domainmodels.user;

import ch.course223.advanced.core.ExtendedDTO;

public class UserDTO extends ExtendedDTO {

    private String firstName;

    private String lastName;

    private String userName;

    public UserDTO() {
    }

    public UserDTO(String firstName, String lastName, String userName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
