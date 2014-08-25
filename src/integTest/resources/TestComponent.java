package org.foo.bar;

import org.osgi.service.component.annotations.Component;

@Component(
        immediate = true,
        service = String.class
)
public class TestComponent {
}