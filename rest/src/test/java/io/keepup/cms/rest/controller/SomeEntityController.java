package io.keepup.cms.rest.controller;

import io.keepup.cms.core.service.AbstractEntityOperationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("rest-test")
public class SomeEntityController extends AbstractRestController<SomeEntity> {
    public SomeEntityController(AbstractEntityOperationService<SomeEntity> operationService) {
        super(operationService);
    }
}
