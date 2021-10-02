msa-order-dev
==============
(수강 강의 - 패스트캠퍼스, The RED : 비즈니스 성공을 위한 Java/Spring 기반 서비스 개발과 MSA 구축 by 이희창)

이 repository는 msa, ddd를 학습하는 곳입니다.  

# 애플리케이션 레이어 구조
|레이어|설명|주요 객체|
|------|---|---|
|사용자 인터페이스(interfaces)|사용자에게 정보를 보여주고 사용자의 명령을 해석한다.|Controller, Dto, Mapper(Converter)|
|응용 계층(application)|수행할 작업을 정의하고 표현력 있는 도메인 객체가 문제를 해결하게 한다. 이 계층에서 책임지는 작업은 업무상 중요하거나 다른 시스템의 응용 계층과 상호 작용하는 데 필요한 것들이다. 이 계층은 얇게 유지되고, 오직 작업을 조정하고 아래에 위치한 계층에 포함된 도메인 객체의 협력자에게 작업을 위임한다.|Facade|
|도메인 계층(domain)|업무 개념과 업무 상황에 대한 정보, 업무 규칙을 표현하는 일을 책임진다. 이 계층에서는 업무상황을 반영하는 상태를 제어하고 사용하며, 그와 같은 상태 저장과 관련된 기술적인 세부사항은 인프라스트럭쳐에 위임한다. 이 계층이 업무용 소프트웨어의 핵심이다.|Entity, Service, Command, Criteria, Info,Reader, Store, Executor, Factory(interface)|
|인프라 스트럭쳐 계층(infrastructure)|상위 계층을 지원하는 일반화된 기술적 기능을 제공한다. 이러한 기능에는 애플리케이션에 대한 메시지 전송, 도메인 영속화, UI 에 위젯을 그리는 것 등이 있다.|low level 구현체(ReaderImpl,StoreImpl, Spring Data JPA, RedisConnector)|

![DDD-layer2](https://user-images.githubusercontent.com/55550753/129905407-8aba8cab-a6ca-4d8b-b9dc-54ff752919b2.PNG)  

# Domain Layer 구현 규칙
1. domain layer에서의 Service는 해당 도메인의 전체 흐름을 파악할 수 있도록 구현한다.
   > 도메인 로직은 **순서**가 제일 중요한 관심사이다. 세세한 로직은 다른 레이어에 맡긴다.
2. domain layer의 Service는 도메인의 흐릉을 제어하는 역할을 맡는다.
   > 따라서 Service는 큰 흐름을 보여줄 수 있도록 도메인당 **하나**로 유지해야 한다.  
   구조가 복잡해질수 있으므로 Service간의 참조 관계는 지양하도록 한다.  

# Infrastructure Layer 구현 규칙
1. dip 사용
   > studyground domainDesign(깃북)참고
2. domain layer와 달리 infrastructure layer의 구현체 간에는 참조를 허용
   
# Application Layer 구현 규칙
1. transaction을 사용하는 도메인 로직과, 그렇지 않은 로직을 관리한다. 
   > 로직을 가볍게 유지해야 한다.
2. 이 프로젝트에서는 XxxFacade로 사용했다.
   > facade는 '건물의 정면'이라는 뜻으로 이 레이어가 하는 역할이, 이 레이어의 정면을 응시했을 때  
   어떤 구조로 코드가 짜여있나 볼 수 있기 때문에 그렇게 지은 것 같다.

예시

```text
public String completeOrder(OrderCommand.RegisterOrder registerOrder){
    var orderToken=orderService.completeOrder(registerOrder);
    notificationService.sendKakao("ORDER_COMPLETE", "content");
    return orderToken;
}
```
- orderService의 completeOrder는 transaction으로 묶어 데이터 정합성에 이슈가 없도록 한다.  
- 다른 서비스의 sendKakao 메소드를 사용해 기능을 구현한다.  
- orderService와 notificationService는 다른 도메인이다.  
- completeOrder와 sendKakao를 분리하여 개별 transaction으로 처리해 sendKakao가 실패해도,  
  completeOrder가 성공하면 주문완료처리는 롤백되지 않는다.

# Interfaces Layer 구현 규칙
1. api 설계 시, request parameter와 response는 최소한으로 줄인다.  
   > api오픈 시 프론트팀에서 그 api를 사용하게 될 텐데, 백엔드팀에서 불필요하다고 판단해 제거할 수는 없는 노릇.  
2. http, gRPC, 비동기 메시징과 같은 서비스간 통신 기술은 Interfacecs layer에서만 사용되도록 한다.  
   > json 처리 관련 로직이나 http cookie 파싱 로직 등이 Domain layer 에서 사용되는 식의 구현은 피해야 한다.  
   그렇게 하지 않으면 언제든지 교체될 수 있는 외부 통신 기술로 인해 domain 로직까지 변경되어야 하는 상황이 발생한다.
3. 요구하는 request 가 많다는 것은 해당 메서드나 객체에서 처리해야 하는 로직이 많다는 것을 의미한다.
   > 이는 해당 객체가 생각보다 많은 역할을 하고 있다는 신호일 수 있다  
   메소드의 역할을 나누어야 한다.  

예시

if가 존재할 메소드(지양)
```text
// isAgreement가 true이면 동의처리, false면 거부처리
public void processReceiveAgreement(String userId, boolean isAgreement){
}
```
if가 존재하지 않을 메소드(추천)
```text
public void receiveAgreement(String userId){
}
public void receiveDisAgreement(String userId){
}
```
