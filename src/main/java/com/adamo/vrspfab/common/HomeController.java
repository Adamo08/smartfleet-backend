package com.adamo.vrspfab.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
@Tag(name = "Home", description = "Basic home endpoint for testing API reachability")
public class HomeController {


    @Operation(summary = "Get a simple greeting",
               description = "Returns a 'Hello From Home' message.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully returned greeting"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping
    public String index(){
        return "Hello From Home";
    }
}
