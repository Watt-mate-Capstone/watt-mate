import React, { useRef, useState } from 'react';

const KAKAO_JS_KEY = 'ff65cc948f3f20894a05032f0024e3b3';

export default function LocationSelector({
  region,
  onChangeRegion,
  onChangeLocation,
}) {
  const mapRef = useRef(null);

  const [showMap, setShowMap] = useState(false);
  const [nearbyRegions, setNearbyRegions] = useState([]);
  const [isFindingRegions, setIsFindingRegions] = useState(false);

  const loadKakaoMapScript = () => {
    return new Promise((resolve, reject) => {
      if (window.kakao && window.kakao.maps) {
        resolve();
        return;
      }

      const existingScript = document.getElementById('kakao-map-script');

      if (existingScript) {
        existingScript.onload = () => resolve();
        existingScript.onerror = () =>
          reject(new Error('카카오맵 SDK 로드 실패'));
        return;
      }

      const script = document.createElement('script');
      script.id = 'kakao-map-script';
      script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_JS_KEY}&autoload=false&libraries=services`;
      script.async = true;

      script.onload = () => resolve();
      script.onerror = () => reject(new Error('카카오맵 SDK 로드 실패'));

      document.head.appendChild(script);
    });
  };

  const drawKakaoMap = (lat, lon) => {
    const mapContainer = mapRef.current;

    if (!mapContainer) {
      alert('지도 영역을 찾을 수 없습니다.');
      return;
    }

    if (!window.kakao || !window.kakao.maps) {
      alert('카카오맵 SDK가 없습니다.');
      return;
    }

    mapContainer.innerHTML = '';

    window.kakao.maps.load(() => {
      const currentPosition = new window.kakao.maps.LatLng(lat, lon);

      const mapOption = {
        center: currentPosition,
        level: 3,
      };

      const map = new window.kakao.maps.Map(mapContainer, mapOption);

      const marker = new window.kakao.maps.Marker({
        position: currentPosition,
      });

      marker.setMap(map);

      const infowindow = new window.kakao.maps.InfoWindow({
        content: '<div style="padding:8px;font-size:14px;">현재 위치</div>',
      });

      infowindow.open(map, marker);

      setTimeout(() => {
        map.relayout();
        map.setCenter(currentPosition);
      }, 100);
    });
  };

  const coordToRegionName = (lat, lon) => {
    return new Promise((resolve) => {
      window.kakao.maps.load(() => {
        const geocoder = new window.kakao.maps.services.Geocoder();

        geocoder.coord2RegionCode(lon, lat, (result, status) => {
          if (status !== window.kakao.maps.services.Status.OK) {
            resolve(null);
            return;
          }

          const adminRegion =
            result.find((item) => item.region_type === 'H') || result[0];

          if (!adminRegion) {
            resolve(null);
            return;
          }

          resolve({
            name: adminRegion.region_3depth_name,
            fullName: `${adminRegion.region_1depth_name} ${adminRegion.region_2depth_name} ${adminRegion.region_3depth_name}`,
            lat,
            lon,
          });
        });
      });
    });
  };

  const makeNearbyCoordinates = (lat, lon) => {
    const distances = [0, 400, 800, 1200, 1600];

    const directions = [
      [0, 0],
      [1, 0],
      [-1, 0],
      [0, 1],
      [0, -1],
      [1, 1],
      [1, -1],
      [-1, 1],
      [-1, -1],
      [2, 0],
      [-2, 0],
      [0, 2],
      [0, -2],
    ];

    const latMeter = 111320;
    const lonMeter = 111320 * Math.cos((lat * Math.PI) / 180);

    const coords = [];

    distances.forEach((distance) => {
      directions.forEach(([dx, dy]) => {
        const newLat = lat + (dy * distance) / latMeter;
        const newLon = lon + (dx * distance) / lonMeter;

        coords.push({
          lat: newLat,
          lon: newLon,
        });
      });
    });

    return coords;
  };

  const findNearbyRegions = async (lat, lon) => {
    const coords = makeNearbyCoordinates(lat, lon);

    const results = await Promise.all(
      coords.map((coord) => coordToRegionName(coord.lat, coord.lon))
    );

    const regionMap = new Map();

    results
      .filter(Boolean)
      .forEach((item) => {
        if (item.name && !regionMap.has(item.name)) {
          regionMap.set(item.name, item);
        }
      });

    return Array.from(regionMap.values());
  };

  const handleSelectRegion = (selectedRegion) => {
    onChangeRegion(selectedRegion.name);
  };

  const showCurrentLocationMap = async () => {
    if (!navigator.geolocation) {
      alert('이 브라우저에서는 위치 정보를 지원하지 않습니다.');
      return;
    }

    try {
      await loadKakaoMapScript();

      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const lat = position.coords.latitude;
          const lon = position.coords.longitude;

          onChangeLocation({
            lat,
            lon,
            region: '',
          });

          setNearbyRegions([]);
          setShowMap(true);

          setTimeout(() => {
            drawKakaoMap(lat, lon);
          }, 300);

          setIsFindingRegions(true);

          try {
            const regions = await findNearbyRegions(lat, lon);
            setNearbyRegions(regions);
          } catch (error) {
            console.error('주변 동네 조회 오류:', error);
            alert('주변 동네를 불러오지 못했습니다.');
          } finally {
            setIsFindingRegions(false);
          }
        },
        (error) => {
          console.log(error);

          if (error.code === 1) {
            alert('위치 권한이 거부되었습니다. 브라우저에서 위치 권한을 허용해주세요.');
          } else if (error.code === 2) {
            alert('현재 위치를 찾을 수 없습니다.');
          } else if (error.code === 3) {
            alert('위치 정보를 가져오는 시간이 초과되었습니다.');
          } else {
            alert('위치 정보를 가져오는 중 오류가 발생했습니다.');
          }
        }
      );
    } catch (error) {
      console.error(error);
      alert('카카오맵 SDK를 불러오지 못했습니다. API 키와 도메인을 확인해주세요.');
    }
  };

  return (
    <>
      <button
        type="button"
        className="signupform-location-btn"
        onClick={showCurrentLocationMap}
      >
        현재 위치로 동네 찾기
      </button>

      {showMap && (
        <div className="signupform-map-box">
          <div ref={mapRef} className="signupform-map"></div>
        </div>
      )}

      {isFindingRegions && (
        <p className="signupform-location-text">
          주변 동네를 찾는 중입니다...
        </p>
      )}

      {nearbyRegions.length > 0 && (
        <div className="signupform-region-box">
          <p className="signupform-region-title">지금 있는 동네</p>

          <div className="signupform-region-list">
            {nearbyRegions.map((item) => (
              <button
                type="button"
                key={item.fullName}
                className={
                  region === item.name
                    ? 'signupform-region-item active'
                    : 'signupform-region-item'
                }
                onClick={() => handleSelectRegion(item)}
              >
                <span>{item.name}</span>
              </button>
            ))}
          </div>
        </div>
      )}

      {region && (
        <p className="signupform-location-text">
          선택한 동네: {region}
        </p>
      )}
    </>
  );
}