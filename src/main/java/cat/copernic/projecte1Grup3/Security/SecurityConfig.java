/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Security;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuració central de Spring Security per a l'aplicació: Gestiona el
 * control d'accés basat en rols (ADMIN, AGENT, CLIENT), la protecció de rutes
 * HTTP i el flux d'inici/tancament de sessió. Defineix també la gestió d'errors
 * d'autorització (403), la memòria cau de peticions prèvies i l'encriptació de
 * contrasenyes.
 *
 * @author Grup3
 */
@Configuration
@EnableMethodSecurity // Habilita las anotaciones @PreAuthorize en los controladores (ej. AgentController)
public class SecurityConfig {

    /**
     * Configura la cadena de filtres de seguretat i les regles d'autorització
     * HTTP: Defineix les rutes públiques, les restriccions per rol per al
     * panell de control i l'àrea de client, i el formulari de login/logout.
     * Estableix els gestors personalitzats per a errors d'autenticació (usuari
     * no validat) i accessos denegats (403).
     */
    // ============================================================
    // BLOQUE 1: CADENA DE FILTROS Y REGLAS DE AUTORIZACIÓN HTTP
    // ============================================================
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                // 1. Recursos estáticos y hojas de estilo públicos
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers("/*.css", "/**/*.css").permitAll()
                // 2. Páginas públicas accesibles sin iniciar sesión
                .requestMatchers("/", "/home", "/login", "/forgot-password", "/registre", "/cataleg").permitAll()
                // 3. Recursos multimedia y de sistema: permitimos acceso libre para evitar
                // que Spring Security los intercepte y los guarde en caché como "página previa"
                .requestMatchers("/error", "/favicon.ico", "/uploads/**", "/images/**").permitAll()
                // 4. RESTRICCIÓN DE AGENTES (SOLO ADMIN):
                // Debe ir ANTES de "/dashboard/**" porque Spring evalúa las reglas de arriba abajo.
                // Impide que un AGENT pueda registrar, editar o dar de baja otros agentes.
                .requestMatchers("/dashboard/agents/alta",
                        "/dashboard/agents/editar/**",
                        "/dashboard/agents/eliminar/**").hasRole("ADMIN")
                // 5. Panel de control interno (incluye la API en tiempo real /dashboard/api/stats)
                .requestMatchers("/dashboard/**").hasAnyRole("ADMIN", "AGENT")
                // 6. Área privada exclusiva para clientes
                .requestMatchers("/reserves-client/**").hasRole("CLIENT")
                // 7. Cualquier otra ruta no especificada requiere estar autenticado
                .anyRequest().authenticated())
                // Guarda en sesión la última página visitada antes de pedir login
                .requestCache(cache -> cache.requestCache(requestCache()))
                // Configuración del formulario de inicio de sesión personalizado
                .formLogin(form -> form
                .loginPage("/login")
                .successHandler(customSuccessHandler(requestCache())) // Redirección dinámica según rol
                .failureHandler((request, response, exception) -> {
                    if (exception instanceof DisabledException) {
                        response.sendRedirect(request.getContextPath() + "/login?notValidated");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/login?error");
                    }
                })
                .permitAll())
                // Configuración del cierre de sesión
                .logout(logout -> logout
                .logoutSuccessUrl("/home")
                .permitAll())
                // ============================================================
                // BLOQUE 2: GESTIÓN DE ACCESOS DENEGADOS (403 / REDIRECCIONES)
                // ============================================================
                .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler((request, response, exception) -> {
                    String ctx = request.getContextPath();
                    String uri = request.getRequestURI();

                    if (uri.startsWith(ctx + "/dashboard")) {
                        // Si un CLIENT intenta entrar al panel de administración, se le redirige al inicio
                        // (Nota: si un AGENT intenta entrar a /dashboard/agents/alta, también entrará aquí)
                        response.sendRedirect(ctx + "/");
                    } else if (uri.startsWith(ctx + "/reserves-client")) {
                        // Si un ADMIN o AGENT intenta entrar a la zona privada de clientes, vuelve al dashboard
                        response.sendRedirect(ctx + "/dashboard");
                    } else {
                        // Para cualquier otro caso no autorizado, devuelve error HTTP 403 Forbidden
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    }
                }));

        return http.build();
    }

    /**
     * Crea el bean per emmagatzemar en sessió les peticions HTTP prèvies al
     * login: Utilitza HttpSessionRequestCache per recordar la URL protegida que
     * l'usuari intentava visitar abans d'autenticar-se.
     */
    // ============================================================
    // BLOQUE 3: CACHÉ DE PETICIONES PREVIAS
    // ============================================================
    @Bean
    RequestCache requestCache() {
        return new HttpSessionRequestCache();
    }

    /**
     * Defineix el gestor de redirecció intel·ligent un cop iniciada la sessió
     * amb èxit: Envia els administradors i agents directament al dashboard,
     * mentre que als clients els retorna a la seva URL prèvia vàlida o al
     * catàleg.
     */
    // ============================================================
    // BLOQUE 4: REDIRECCIÓN INTELIGENTE TRAS INICIAR SESIÓN
    // ============================================================
    @Bean
    AuthenticationSuccessHandler customSuccessHandler(RequestCache requestCache) {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            String contextPath = request.getContextPath();

            // 1. Si el usuario tiene rol ADMIN o AGENT, entra directamente al Dashboard
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_ADMIN") || role.equals("ROLE_AGENT")) {
                    response.sendRedirect(contextPath + "/dashboard");
                    return;
                }
            }

            // 2. Si es CLIENT: recuperamos la URL que intentaba visitar antes del login,
            // filtrando que sea una página web real y no una imagen, icono o ruta de error
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null) {
                String targetUrl = savedRequest.getRedirectUrl();
                boolean esPaginaValida = !targetUrl.contains("/error")
                        && !targetUrl.contains("/uploads/")
                        && !targetUrl.contains("/images/")
                        && !targetUrl.contains("favicon");
                if (esPaginaValida) {
                    response.sendRedirect(targetUrl);
                    return;
                }
            }

            // 3. Si es CLIENT y no tenía ninguna página previa guardada, va al catálogo por defecto
            response.sendRedirect(contextPath + "/cataleg");
        };
    }

    /**
     * Defineix el codificador de contrasenyes basat en l'algorisme BCrypt:
     * Proporciona el bean PasswordEncoder utilitzat per Spring Security i els
     * serveis d'usuaris per xifrar i verificar credencials.
     */
    // ============================================================
    // BLOQUE 5: ENCRIPTACIÓN DE CONTRASEÑAS
    // ============================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Utiliza el algoritmo BCrypt para encriptar y verificar contraseñas de forma segura
        return new BCryptPasswordEncoder();
    }
}
