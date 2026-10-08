/*
 * #%L
 * LoadUp Web MVC
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.webmvc;

import io.github.loadup.commons.context.ExecutionContext;
import io.github.loadup.commons.context.ServiceTemplate;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/** Binds immutable request metadata for each servlet dispatch. */
public final class ExecutionContextFilter extends OncePerRequestFilter {
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String attribute = ExecutionContext.class.getName();
        ExecutionContext context =
                request.getAttribute(attribute) instanceof ExecutionContext saved ? saved : ExecutionContext.empty();
        request.setAttribute(attribute, context);
        try {
            ServiceTemplate.execute(context, () -> {
                chain.doFilter(request, response);
                return null;
            });
        } catch (IOException | ServletException | RuntimeException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new ServletException(failure);
        }
    }
}
