package com.epam.brn.model

import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.dto.response.UserWithAnalyticsDto
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany

@Entity
data class UserAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val userId: String? = null,
    @Column(nullable = false, unique = true)
    val email: String?,
    var fullName: String?,
    val password: String?,
    var bornYear: Int?,
    var gender: String?,
    var active: Boolean = true,
    @Column(nullable = false)
    var created: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    @Column(nullable = false)
    var changed: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    var avatar: String? = null,
    var foto: String? = null,
    var description: String? = null,
    @OneToMany
    @JoinColumn(name = "doctor", referencedColumnName = "id")
    var patients: MutableList<UserAccount> = mutableListOf()
) {
    @ManyToMany(cascade = [(CascadeType.MERGE)])
    @JoinTable(
        name = "user_authorities",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "authority_id", referencedColumnName = "id")]
    )
    var authoritySet: MutableSet<Authority> = hashSetOf()

    override fun toString(): String {
        return "UserAccount(id=$id, userId=$userId, fullName='$fullName', email='$email'," +
            " bornYear=$bornYear, gender=$gender, description=$description)"
    }

    fun toDto(): UserAccountDto {
        val userAccountDto = UserAccountDto(
            id = id,
            userId = userId,
            name = fullName,
            active = active,
            email = email,
            bornYear = bornYear,
            gender = gender?.let { Gender.valueOf(it) },
            created = created,
            changed = changed,
            avatar = avatar,
            foto = foto,
            description = description,
            patients = patients
                .map(UserAccount::toDto)
                .toMutableList()
        )
        userAccountDto.authorities = this.authoritySet
            .map(Authority::authorityName)
            .toMutableSet()
        return userAccountDto
    }

    fun toAnalyticsDto() = UserWithAnalyticsDto(
        id = id,
        userId = userId,
        name = fullName,
        active = active,
        email = email,
        bornYear = bornYear,
        gender = gender?.let { Gender.valueOf(it) },
    )
}
