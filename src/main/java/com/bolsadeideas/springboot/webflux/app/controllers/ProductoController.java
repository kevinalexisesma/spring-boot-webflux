package com.bolsadeideas.springboot.webflux.app.controllers;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;

import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@SessionAttributes("producto")
@Controller
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @GetMapping({ "/listar", "/" })
    public String listar(Model model) {
        Flux<Producto> productos = productoService.findAllConNombreUpperCase();

        productos.subscribe(prod -> log.info(prod.getNombre()));
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de Productos");
        return "listar";
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Formulario de Producto");
        return Mono.just("form");
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model model) {
        Mono<Producto> producto = productoService.findById(id)
                .doOnNext(prod -> log.info(prod.getNombre()))
                .defaultIfEmpty(new Producto());

        model.addAttribute("producto", producto);
        model.addAttribute("titulo", "Editar Producto");
        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> guardar(Producto product, SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return productoService.save(product)
                .doOnNext(prod -> log.info("Producto guardado: " + prod.getNombre()))
                .thenReturn("redirect:/listar");
    }

    @GetMapping("/listar-dataDriver")
    public String listarDataDriver(Model model) {
        Flux<Producto> productos = productoService.findAllConNombreUpperCase()
                .delayElements(Duration.ofSeconds(1));

        productos.subscribe(prod -> log.info(prod.getNombre()));
        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
        model.addAttribute("titulo", "Listado de Productos");
        return "listar";
    }

    @GetMapping("/listar-full")
    public String listarFull(Model model) {
        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de Productos");
        return "listar";
    }

    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {
        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de Productos");
        return "listar-chunked";
    }
}
