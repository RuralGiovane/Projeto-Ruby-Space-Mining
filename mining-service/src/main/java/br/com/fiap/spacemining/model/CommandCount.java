package br.com.fiap.spacemining.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "command_count")
public class CommandCount {
    @Id
    @Column(length = 5)
    private String command;

    @Column(nullable = false)
    private long total;

    protected CommandCount() {
    }

    public String getCommand() {
        return command;
    }

    public long getTotal() {
        return total;
    }

    public void increment() {
        total++;
    }
}
