package com.rodemtree.yeyakitda.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@NoArgsConstructor
@Document("restaurantOperatingHours")
public class RestaurantOperatingHours {

    @Id
    private String id;

    @Field("restaurantId")
    private Long restaurantId;

    @Field("operatingHours")
    private List<OperatingHour> operatingHours;

    private RestaurantOperatingHours(Long restaurantId, List<OperatingHour> operatingHours) {
        this.restaurantId = restaurantId;
        this.operatingHours = operatingHours;
    }

    public static RestaurantOperatingHours of(Long restaurantId, List<OperatingHour> operatingHours) {
        return new RestaurantOperatingHours(restaurantId, operatingHours);
    }
}
