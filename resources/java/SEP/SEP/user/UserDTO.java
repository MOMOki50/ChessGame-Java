package SEP.SEP.user;

public class UserDTO {
    private long id;
    private String email;
    private String firstname;
    private String lastname;
    private byte[] profilePicture;

    // Standardkonstruktor, Getter und Setter
    public UserDTO() {}

    public UserDTO(long id, String email, String firstname, String lastname, byte[] profilePicture) {
        this.id = id;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.profilePicture = profilePicture;
    }

    public static UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstname(),
                user.getLastname(),
                user.getProfilePicture()
        );
    }

    // Getter und Setter
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }
}