package br.com.AllTallent.controller;

import br.com.AllTallent.dto.AvaliacaoDetalhadaDTO;
import br.com.AllTallent.dto.AvaliacaoFuncionarioResponseDTO;
import br.com.AllTallent.dto.AvaliacaoParaResponderDTO;
import br.com.AllTallent.dto.AvaliacaoRequestDTO;
import br.com.AllTallent.dto.AvaliacaoResponseDTO;
import br.com.AllTallent.dto.RespostaColaboradorRequestDTO;
import br.com.AllTallent.dto.RespostaColaboradorResponseDTO;
import br.com.AllTallent.dto.RevisaoDetalhadaDTO;
import br.com.AllTallent.dto.RevisaoSupervisorRequestDTO;
import br.com.AllTallent.exception.ResourceNotFoundException;
import br.com.AllTallent.exception.UnauthorizedActionException;
import br.com.AllTallent.service.AvaliacaoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<AvaliacaoResponseDTO> criarAvaliacao(@Valid @RequestBody AvaliacaoRequestDTO dto) {
        try {
            AvaliacaoResponseDTO avaliacaoCriada = avaliacaoService.criarAvaliacaoCompleta(dto);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(avaliacaoCriada.codigo())
                    .toUri();

            return ResponseEntity.created(location).body(avaliacaoCriada);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.err.println("Erro ao criar avaliação: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<List<AvaliacaoResponseDTO>> listarTodasAvaliacoes() {
        List<AvaliacaoResponseDTO> lista = avaliacaoService.listarTodasAvaliacoes();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<AvaliacaoDetalhadaDTO> buscarAvaliacaoDetalhada(@PathVariable Integer id) {
        try {
            AvaliacaoDetalhadaDTO detalhadaDTO = avaliacaoService.buscarAvaliacaoDetalhada(id);
            return ResponseEntity.ok(detalhadaDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/instancias")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<List<AvaliacaoFuncionarioResponseDTO>> buscarInstanciasPorAvaliacao(@PathVariable Integer id) {
        try {
            List<AvaliacaoFuncionarioResponseDTO> instancias = avaliacaoService.buscarInstanciasPorAvaliacao(id);
            return ResponseEntity.ok(instancias);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/respostas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> salvarResposta(@Valid @RequestBody RespostaColaboradorRequestDTO respostaDTO) {
        try {
            RespostaColaboradorResponseDTO respostaSalva = avaliacaoService.salvarOuAtualizarResposta(respostaDTO);
            return ResponseEntity.ok(respostaSalva);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("Erro ao salvar resposta: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro ao salvar resposta: " + e.getMessage());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro interno ao salvar resposta: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao salvar resposta.");
        }
    }

    @GetMapping("/instancias/{instanciaId}/respostas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<?> buscarRespostasPorInstancia(@PathVariable Long instanciaId) {
        try {
            List<RespostaColaboradorResponseDTO> respostas = avaliacaoService.buscarRespostasPorInstancia(instanciaId);
            return ResponseEntity.ok(respostas);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/revisao/{codigoAvaliacaoFuncionario}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<?> getDadosParaRevisao(@PathVariable Long codigoAvaliacaoFuncionario) {
        try {
            List<RevisaoDetalhadaDTO> revisao = avaliacaoService.buscarDadosRevisao(codigoAvaliacaoFuncionario);
            return ResponseEntity.ok(revisao);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/instancias/{instanciaId}/revisar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<?> salvarRevisaoSupervisor(
            @PathVariable Long instanciaId,
            @Valid @RequestBody RevisaoSupervisorRequestDTO revisaoDTO) {
        try {
            AvaliacaoFuncionarioResponseDTO instanciaAtualizada = avaliacaoService.salvarRevisaoSupervisor(instanciaId, revisaoDTO);
            return ResponseEntity.ok(instanciaAtualizada);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro interno ao salvar revisão: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao salvar revisão.");
        }
    }

    @GetMapping("/pendentes/{funcionarioId}")
    @PreAuthorize("principal.codigo == #funcionarioId")
    public ResponseEntity<List<AvaliacaoFuncionarioResponseDTO>> buscarAvaliacoesPendentes(@PathVariable Integer funcionarioId) {
        List<AvaliacaoFuncionarioResponseDTO> pendentes = avaliacaoService.buscarPendentesPorFuncionario(funcionarioId);
        return ResponseEntity.ok(pendentes);
    }

    @GetMapping("/instancias/{instanciaId}/responder")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> buscarAvaliacaoParaResponder(@PathVariable Long instanciaId) {
        try {
            AvaliacaoParaResponderDTO dto = avaliacaoService.buscarParaResponder(instanciaId);
            return ResponseEntity.ok(dto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/instancias/{instanciaId}/finalizar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> finalizarAvaliacaoColaborador(@PathVariable Long instanciaId) {
        try {
            avaliacaoService.finalizarPeloColaborador(instanciaId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
