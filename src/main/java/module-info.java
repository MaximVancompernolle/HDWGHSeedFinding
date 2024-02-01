module com.example.hdwghseedfinding {
    requires javafx.controls;
    requires javafx.fxml;
            requires org.apache.groovy;
        
                            
    opens com.example.hdwghseedfinding to javafx.fxml;
    exports com.example.hdwghseedfinding;
}