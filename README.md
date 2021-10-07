msa-order-dev
==============
(수강 강의 - 패스트캠퍼스, The RED : 비즈니스 성공을 위한 Java/Spring 기반 서비스 개발과 MSA 구축 by 이희창)

repository 주제 : ddd, msa 구현

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

if가 존재할 메소드(**지양**)
```text
// isAgreement가 true이면 동의처리, false면 거부처리
public void processReceiveAgreement(String userId, boolean isAgreement){
}
```
if가 존재하지 않을 메소드(**추천**)
```text
public void receiveAgreement(String userId){
}
public void receiveDisAgreement(String userId){
}
```
# 프로젝트의 도메인은 파트너, 상품, 주문 총 3개로 구성

![domaindiagram](https://user-images.githubusercontent.com/55550753/135727190-5712c26f-208a-438c-81f5-c5d616e2f547.PNG)

# 프로젝트 세부 구현
![ddd-specific-layer](https://user-images.githubusercontent.com/55550753/136355644-4146b1ea-57b3-43fa-bedc-1ba080f159d1.PNG)
1. 단일 엔티티(파트너)와 aggregate root(Item, Order)에는 토큰값을 사용하는 대체키 구현
> 보안 향상을 위해 사용
> 외부에 api오픈 시 대체키 사용, 내부적으로 호출할때는 id(PK) 사용해도 상관없음
2. created_at, updated_at은 공통적으로 사용하는 부분이 많으므로 @MappedSuperClass를 선언해 상속해서 사용
> 시간은 글로벌 진출도 고려한 확장성을 위해 LocalDateTime 대신   
> ZonedDateTime에 @CreationTimeStamp를 선언해 사용한다.
```text
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AbstractEntity {

    @CreationTimestamp
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}
```
3. Service 인터페이스를 구현한 ServiceImpl 내부에서도 내부 로직의 추상화 정도를 높여서   
   인터페이스 메소드를 호출해 사용한다.  
   ex) Store, Reader 인터페이스 메소드 호출로 구현체 메소드 사용  

**얻을 수 있는 이점**  
> a) 추상화를 높인 내부 서비스 코드는 코드가 읽기 쉬워진다. 유지보수가 쉬워지는 것은 덤.  
> b) 로직 수정이 필요할 때 도메인 코드는 건드리지 않고 infrastructure 레이어의 코드만 수정하면 된다.   

4. CommonResponse를 사용해 응답값 규격화  
5. ControllerAdvice와 직접 정의한 exception 를 사용해 미리 예상된 예외 처리를 가능하게 함  
> 이 과정에서 개발자가 직접 정의하지 않은 exception에 대해서는 모니터링이 필요한 부분이다.  
6. Aggregate 외부에서는 Aggregate root(Aggregate당 1개만 존재)를 제외한 내부 Aggregate 요소들을 참조할 수 없다.
> Item 을 획득하면 Aggregate 내부의 객체를 탐색해서 획득할 수 있게 된다
7. Aggregate 내부 요소들을 인스턴스화할 때 Factory를 사용한다.
> 자신의 책임과 역할이 다른 객체를 생성하는 프로그램 요소를 Factory 라고 하는데,  
복잡한 객체와 Aggregate 인스턴스를 생성하는 책임을 맡기기에 적합하다
8. MapStruct를 사용해 레이어간 전달 객체 타입 변환 간소화
> https://mapstruct.org/ (MapStruct in 2 minutes) <- 세부 사용법 참고  
9. 보상 트랜잭션 최소화
> 보상 트랜잭션이란 이전에 커밋된 트랜잭션을 취소하는 것인데 데이터베이스 롤백과 일치하는 경우가 존재한다.  
> 보상 트랜잭션 또한 추가적인 로직이므로 최소한으로 하는 것이 좋다.  
> ex) 결제 완료 -> 포인트 지급 -> 배송 준비중   
배송 준비중의 경우 상품 품절로 인한 예외가 자주 발생할 수 있다고 가정.    
결제 완료 → 배송 준비중 → 포인트 지급 이라는 비즈니스 순서만   
바꾸어도 배송 준비중 과정에서 예외가 발생하더라도 포인트 지급에 대한 보상트랜잭션,   
> 즉 포인트 회수라는 프로세스 실행은 하지 않아도 되는 로직 개선이 이루어지게 된다.    
> **OrderServiceImpl의 paymentOrder메소드 참고**  
10. 엔티티에서 필수적으로 필요한 필드 또는 항상 같이 존재해야 하는 필드는 @Embedded, @Embeddable을 사용해 객체로 관리한다.
> Order Entity의 DeliveryFragment를 참고
11. 주문(Order) 도메인의 가격 계산은 서비스가 아닌 엔티티에서 이루어진다.  
> Order -> OrderItem -> OrderItemOptionGroup -> OrderItemOption 구조에서    
> 엔티티에 메소드를 만드는 것만으로 해결가능하다.    
> (엔티티간 연관관계는 위에서 소개한 Order 도메인 엔티티를 참고)   
OrderItemOption에서 - 옵션 선택   
OrderItemOptionGroup - 옵션 그룹 : calculateTotalAmount()로 선택된 옵션의 금액합계 구함   
OrderItem - 옵션 그룹과 옵션이 선택되어있는 주문상품 : calculateTotalAmount()로 옵션 그룹의 금액합계 구함   
Order - 주문 : calculateTotalAmount()로 주문한 1개 이상의 주문상품에 대한 금액합계를 구함  
12. Spring이 제공하는 DI를 활용해 각각의 인터페이스를 구현한 구현체를 List 로 받아 활용한다
> Order 도메인 layer 구현 후 infrastructure layer의 PaymentProcessorImpl에서 사용    

![orderpaydip](https://user-images.githubusercontent.com/55550753/136385101-1b01a56b-89dd-4681-810d-a349bdaa5c54.PNG)

(**프로젝트 실행은 docker-compose와 flyway를 통해 세팅합니다.**)