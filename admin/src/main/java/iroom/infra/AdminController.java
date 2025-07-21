package iroom.infra;

import iroom.domain.*;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//<<< Clean Arch / Inbound Adaptor

@RestController
// @RequestMapping(value="/admins")
@Transactional
public class AdminController {

    @Autowired
    AdminRepository adminRepository;

    @RequestMapping(
        value = "/admins/register",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8"
    )
    public Admin register(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody RegisterCommand registerCommand
    ) throws Exception {
        System.out.println("##### /admin/register  called #####");
        Admin admin = new Admin();
        admin.register(registerCommand);
        adminRepository.save(admin);
        return admin;
    }

    @RequestMapping(
        value = "/admins/{id}/modifyrole",
        method = RequestMethod.PUT,
        produces = "application/json;charset=UTF-8"
    )
    public Admin modifyRole(
        @PathVariable(value = "id") Long id,
        @RequestBody ModifyRoleCommand modifyRoleCommand,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
        System.out.println("##### /admin/modifyRole  called #####");
        Optional<Admin> optionalAdmin = adminRepository.findById(id);

        optionalAdmin.orElseThrow(() -> new Exception("No Entity Found"));
        Admin admin = optionalAdmin.get();
        admin.modifyRole(modifyRoleCommand);

        adminRepository.save(admin);
        return admin;
    }
}
//>>> Clean Arch / Inbound Adaptor
