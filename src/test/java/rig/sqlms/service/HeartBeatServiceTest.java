package rig.sqlms.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rig.sqlms.config.PackageInfoConfiguration;
import rig.sqlms.config.PackageVersionConfiguration;
import rig.sqlms.dto.HeartBeatInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeartBeatServiceTest {

    @Mock
    private ServerInfoService serverInfoService;
    @Mock
    private PackageInfoConfiguration packageInfoConfiguration;
    @Mock
    private PackageVersionConfiguration packageVersionConfiguration;

    @Test
    void getData_shouldAssembleInfoFromCollaborators() {
        when(packageInfoConfiguration.getAppName()).thenReturn("sql-ms");
        when(packageVersionConfiguration.getBuildTime()).thenReturn(1_700_000_000L);
        when(packageVersionConfiguration.getMajor()).thenReturn("1");
        when(packageVersionConfiguration.getMinor()).thenReturn("2");
        when(packageVersionConfiguration.getPatch()).thenReturn("3");
        when(serverInfoService.getStartupTime()).thenReturn(1_700_000_100L);
        when(serverInfoService.getServerTime()).thenReturn(1_700_000_200L);

        HeartBeatService service = new HeartBeatService(serverInfoService, packageInfoConfiguration, packageVersionConfiguration);
        HeartBeatInfo data = service.getData();

        assertEquals(HeartBeatInfo.builder()
                .appName("sql-ms")
                .packagingTime(1_700_000_000L)
                .version("v1.2.3")
                .appStartTime(1_700_000_100L)
                .serverTime(1_700_000_200L)
                .build(), data);
    }

    @Test
    void getData_shouldComputeVersionOnlyOnce() {
        when(packageVersionConfiguration.getMajor()).thenReturn("1");
        when(packageVersionConfiguration.getMinor()).thenReturn("2");
        when(packageVersionConfiguration.getPatch()).thenReturn("3");

        HeartBeatService service = new HeartBeatService(serverInfoService, packageInfoConfiguration, packageVersionConfiguration);
        service.getData();
        service.getData();

        verify(packageVersionConfiguration, times(1)).getMajor();
        verify(packageVersionConfiguration, times(1)).getMinor();
        verify(packageVersionConfiguration, times(1)).getPatch();
    }
}
