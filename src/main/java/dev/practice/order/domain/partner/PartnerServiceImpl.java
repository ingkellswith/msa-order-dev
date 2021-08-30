package dev.practice.order.domain.partner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {
    private final PartnerStore partnerStore;
    private final PartnerReader partnerReader;

    @Override
    @Transactional
    public PartnerInfo registerPartner(PartnerCommand command) {
        // 1. command -> initPartner
        // 2. initPartner save to DB
        // 3. Partner -> PartnerInfo AND return
        // 1.
        var initPartner = command.toEntity();
        // 2.
        Partner partner = partnerStore.store(initPartner);
        // 3.
        return new PartnerInfo(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerInfo getPartnerInfo(String partnerToken) {
        // 1. partnerToken -> Partner
        // Partner -> PartnerInfo AND return
        Partner partner = partnerReader.getPartner(partnerToken);
        return new PartnerInfo(partner);
    }

    @Override
    @Transactional
    public PartnerInfo enablePartner(String partnerToken) {
        // 1. partnerToken -> Partner
        // partner.enable()
        Partner partner = partnerReader.getPartner(partnerToken);
        partner.enable();
        return new PartnerInfo(partner);
    }

    @Override
    @Transactional
    public PartnerInfo disablePartner(String partnerToken) {
        // 1. partnerToken -> Partner
        // partner.disable()
        Partner partner = partnerReader.getPartner(partnerToken);
        partner.disable();
        return new PartnerInfo(partner);
    }
}
