package com.projectui.config.portauthority;

import java.util.HashMap;
import java.util.Map;

import com.cloudinary.Cloudinary;


// =====================================================
// CLOUDINARY CONFIGURATION
// =====================================================

public class CloudinaryConfig {

    public static Cloudinary cloudinary;


    // =====================================================
    // GET CLOUDINARY INSTANCE
    // =====================================================

    public static Cloudinary getCloudinary() {

        if (cloudinary == null) {

            Map<String, Object> config =
                    new HashMap<>();


            // =================================================
            // CLOUDINARY CREDENTIALS
            // =================================================

            config.put("cloud_name","zab3bobe");

            config.put("api_key","392742456698257");

            config.put("api_secret","u4MvBHIEtppvkebtQ2yf74V4wZU");

            config.put("secure",true);


            cloudinary =new Cloudinary(config);
        }


        return cloudinary;
    }
}