package com.cicdaas.mockemailservice;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<SimpleSmtpMessage, Long> {

    List<SimpleSmtpMessage> findByToOrderByReceivedDateDesc(String to);

    void deleteByTo(String to);

    long countByTo(String to);
}
