package io.github.loadup.components.configcenter.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.components.configcenter.binder.ConfigCenterBinder;
import io.github.loadup.components.configcenter.binding.impl.DefaultConfigCenterBinding;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.components.configcenter.model.ConfigChangeListener;
import io.github.loadup.framework.api.context.BindingContext;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * DefaultConfigCenterBinding 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class DefaultConfigCenterBindingTest {

    @Mock
    @SuppressWarnings("rawtypes")
    private ConfigCenterBinder mockBinder;

    private DefaultConfigCenterBinding binding;
    private ConfigCenterBindingCfg bindingCfg;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeEach
    void setUp() {
        binding = new DefaultConfigCenterBinding();
        bindingCfg = new ConfigCenterBindingCfg();
        bindingCfg.setGroup("DEFAULT_GROUP");

        BindingContext<ConfigCenterBinder, ConfigCenterBindingCfg> ctx =
                new BindingContext<>("default", "local", bindingCfg, List.of(mockBinder), null);
        binding.init(ctx);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getConfig_shouldDelegateToBinderAndReturnValue() {
        when(mockBinder.getConfig(anyString(), anyString(), any())).thenReturn(Optional.of("value123"));

        Optional<String> result = binding.getConfig("my.key");

        assertThat(result).contains("value123");
        verify(mockBinder).getConfig("my.key", "DEFAULT_GROUP", bindingCfg);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getConfig_shouldReturnEmpty_whenBinderReturnsEmpty() {
        when(mockBinder.getConfig(anyString(), anyString(), any())).thenReturn(Optional.empty());

        Optional<String> result = binding.getConfig("missing.key");

        assertThat(result).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishConfig_shouldDelegateToBinderAndReturnTrue() {
        when(mockBinder.publishConfig(anyString(), anyString(), anyString(), any())).thenReturn(true);

        binding.publishConfig("my.key", "new-value");

        verify(mockBinder).publishConfig("my.key", "DEFAULT_GROUP", "new-value", bindingCfg);
    }

    @Test
    @SuppressWarnings("unchecked")
    void removeConfig_shouldDelegateToBinderAndReturnTrue() {
        when(mockBinder.removeConfig(anyString(), anyString(), any())).thenReturn(true);

        boolean result = binding.removeConfig("my.key");

        assertThat(result).isTrue();
        verify(mockBinder).removeConfig("my.key", "DEFAULT_GROUP", bindingCfg);
    }

    @Test
    @SuppressWarnings("unchecked")
    void addListener_shouldWrapListenerAndDelegateToBinder() {
        ConfigChangeListener listener = event -> {};

        binding.addListener("my.key", listener);

        verify(mockBinder).addListener(anyString(), anyString(), any(), any());
    }
}
