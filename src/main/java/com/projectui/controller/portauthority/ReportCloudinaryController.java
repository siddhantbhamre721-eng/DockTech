package com.projectui.controller.portauthority;

import java.io.File;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.projectui.config.portauthority.CloudinaryConfig;


// =====================================================
// REPORT CLOUDINARY CONTROLLER
// =====================================================

public class ReportCloudinaryController {


    // =====================================================
    // UPLOAD PDF REPORT
    // =====================================================

    public Map<String, Object> uploadReport(
            File file
    ) {

        Cloudinary cloudinary =
                CloudinaryConfig.getCloudinary();


        try {

            Map<String, Object> result =
                    cloudinary.uploader()
                            .upload(
                                    file,

                                    ObjectUtils.asMap(

                                            "resource_type",
                                            "raw",

                                            "folder",
                                            "DockTech/Reports"
                                    )
                            );


            System.out.println(
                    "Cloudinary Upload Result:"
            );


            System.out.println(
                    result
            );


            return result;


        } catch (Exception e) {

            e.printStackTrace();
        }


        return null;
    }


    // =====================================================
    // GET SECURE URL
    // =====================================================

    public String getSecureUrl(
            Map<String, Object> result
    ) {

        if (result == null) {

            return null;
        }


        Object url =
                result.get(
                        "secure_url"
                );


        if (url == null) {

            return null;
        }


        return String.valueOf(
                url
        );
    }


    // =====================================================
    // GET CLOUDINARY PUBLIC ID
    // =====================================================

    public String getPublicId(
            Map<String, Object> result
    ) {

        if (result == null) {

            return null;
        }


        Object publicId =
                result.get(
                        "public_id"
                );


        if (publicId == null) {

            return null;
        }


        return String.valueOf(
                publicId
        );
    }


    // =====================================================
    // DELETE PDF REPORT
    // =====================================================

    public boolean deleteReport(
            String publicId
    ) {

        if (publicId == null
                ||
                publicId.trim().isEmpty()) {

            return false;
        }


        Cloudinary cloudinary =
                CloudinaryConfig.getCloudinary();


        try {

            Map<String, Object> result =
                    cloudinary.uploader()
                            .destroy(
                                    publicId,

                                    ObjectUtils.asMap(
                                            "resource_type",
                                            "raw"
                                    )
                            );


            System.out.println(
                    "Cloudinary Delete Result:"
            );


            System.out.println(
                    result
            );


            String status =
                    String.valueOf(
                            result.get(
                                    "result"
                            )
                    );


            return "ok".equalsIgnoreCase(
                    status
            );


        } catch (Exception e) {

            e.printStackTrace();
        }


        return false;
    }
}