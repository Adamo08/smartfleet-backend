package com.adamo.vrspfab.admins;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Panel", description = "Endpoints for accessing the admin panel. Specific functionalities will be in other controllers.")
public class AdminController {
}
